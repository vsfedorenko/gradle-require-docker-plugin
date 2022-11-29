package io.github.meiblorn.requiredocker

import com.bmuschko.gradle.docker.DockerRegistryCredentials
import com.bmuschko.gradle.docker.internal.services.DockerClientService
import com.bmuschko.gradle.docker.tasks.AbstractDockerRemoteApiTask
import com.bmuschko.gradle.docker.tasks.RegistryCredentialsAware
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.kotlin.dsl.withType

open class RequireDockerPlugin : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        val extension = newExtension()
        configureRegistryCredentialsAwareTasks(
            extension.docker.registryCredentials)

        val service = newDockerService(extension)
        tasks.withType(AbstractDockerRemoteApiTask::class).configureEach {
            dockerClientService.set(service)
        }

        afterEvaluate {
            extension.specs.forEach { spec ->
                spec.finalize()
                apply(spec)
            }
        }
    }

    private fun Project.newExtension(): RequireDockerExtension {
        return extensions.create("requireDocker", RequireDockerExtension::class)
    }

    private fun Project.newDockerService(extension: RequireDockerExtension) =
        gradle.sharedServices.registerIfAbsent(
            "docker",
            DockerClientService::class
        ) {
            parameters {
                url.set(extension.docker.url)
                certPath.set(extension.docker.certPath)
                apiVersion.set(extension.docker.apiVersion)
            }
        }

    private fun Project.configureRegistryCredentialsAwareTasks(
        extensionRegistryCredentials: DockerRegistryCredentials
    ) {
        tasks.withType(RegistryCredentialsAware::class).configureEach {
            registryCredentials.url.set(extensionRegistryCredentials.url)
            registryCredentials.username.set(extensionRegistryCredentials.username)
            registryCredentials.password.set(extensionRegistryCredentials.password)
            registryCredentials.email.set(extensionRegistryCredentials.email)
        }
    }

    private fun Project.apply(spec: RequireDockerSpec) {
        val dockerTaskNameGenerator: DockerTaskNameGenerator = DockerTaskNameGeneratorImpl()
        val dockerContainerNameGenerator: DockerContainerNameGenerator = DockerContainerNameGeneratorImpl(this)
        val dockerTaskFactory: DockerTaskFactory =
            DockerTaskFactoryImpl(this, dockerContainerNameGenerator, dockerTaskNameGenerator)

        spec.containers.forEach { container ->
            val pullImageTask =
                dockerTaskFactory.newPullImageTask(container)

            val createContainerTask =
                dockerTaskFactory.newCreateContainerTask(container) {
                    dependsOn(pullImageTask)
                }

            val startContainerTask =
                dockerTaskFactory.newStartContainerTask(
                    container,
                    createContainerTask.flatMap { it.containerId }) {
                    dependsOn(createContainerTask)
                }

            val waitHealthyContainerTask =
                dockerTaskFactory.newWaitHealthyContainerTask(
                    container,
                    startContainerTask.flatMap { it.containerId }) {
                    dependsOn(startContainerTask)
                }

            var lastHealthTask: Task = waitHealthyContainerTask.get()
            container.readinessProbes.get().forEach { probe ->
                var probeTask = when (probe) {
                    is LogMessageReadinessProbe -> {
                        dockerTaskFactory.newWaitLogMessageTask(
                            container,
                            probe,
                            startContainerTask.flatMap { it.containerId }) {
                            dependsOn(lastHealthTask)
                        }
                    }

                    else -> throw IllegalArgumentException("Unknown readiness probe type: ${probe.javaClass}")
                }
                lastHealthTask = probeTask.get()
            }

            val stopContainerTask =
                dockerTaskFactory.newStopContainerTask(
                    container,
                    createContainerTask.flatMap { it.containerId }) {
                    dependsOn(lastHealthTask)
                }

            val removeContainerTask =
                dockerTaskFactory.newRemoveContainerTask(
                    container,
                    createContainerTask.flatMap { it.containerId }) {
                    dependsOn(stopContainerTask)
                }

            spec.tasks.get().forEach { task ->
                task.dependsOn(lastHealthTask)
                task.finalizedBy(removeContainerTask.get())
            }
        }
    }
}
