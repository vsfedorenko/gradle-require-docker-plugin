package io.github.meiblorn.requiredocker

import com.bmuschko.gradle.docker.DockerRegistryCredentials
import com.bmuschko.gradle.docker.internal.services.DockerClientService
import com.bmuschko.gradle.docker.tasks.AbstractDockerRemoteApiTask
import com.bmuschko.gradle.docker.tasks.RegistryCredentialsAware
import io.github.meiblorn.requiredocker.task.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.kotlin.dsl.withType

open class RequireDockerPlugin : Plugin<Project> {

    override fun apply(project: Project) =
        with(project) {
            val extension = newExtension()
            configureRegistryCredentialsAwareTasks(extension.docker.registryCredentials)

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
        gradle.sharedServices.registerIfAbsent("docker", DockerClientService::class) {
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

        val pullImageTaskFactory: PullImageTaskFactory = PullImageTaskFactoryImpl(this)

        val createContainerTaskFactory: CreateContainerTaskFactory =
            CreateContainerTaskFactoryImpl(this)

        val startContainerTaskFactory: StartContainerTaskFactory =
            StartContainerTaskFactoryImpl(this)

        val waitHealthyContainerTaskFactory: WaitHealthyContainerTaskFactory =
            WaitHealthyContainerTaskFactoryImpl(this)

        val waitLogMessageTaskFactory: WaitLogMessageTaskFactory =
            WaitLogMessageTaskFactoryImpl(this)

        val stopContainerTaskFactory: StopContainerTaskFactory = StopContainerTaskFactoryImpl(this)

        val removeContainerTaskFactory: RemoveContainerTaskFactory =
            RemoveContainerTaskFactoryImpl(this)

        spec.containers.forEach { container ->
            val pullImageTask =
                pullImageTaskFactory.create(
                    name = dockerTaskNameGenerator.generate(container, "pull", "Image"),
                    container = container)

            val createContainerTask =
                createContainerTaskFactory
                    .create(
                        name = dockerTaskNameGenerator.generate(container, "create", "Container"),
                        container = container)
                    .also { it.configure { dependsOn(pullImageTask) } }

            val startContainerTask =
                startContainerTaskFactory
                    .create(
                        name = dockerTaskNameGenerator.generate(container, "start", "Container"),
                        containerId = createContainerTask.flatMap { it.containerId })
                    .also { it.configure { dependsOn(createContainerTask) } }

            val waitHealthyContainerTask =
                waitHealthyContainerTaskFactory
                    .create(
                        name =
                            dockerTaskNameGenerator.generate(container, "waitHealthy", "Container"),
                        containerId = createContainerTask.flatMap { it.containerId })
                    .also { it.configure { dependsOn(startContainerTask) } }

            var lastHealthTask: Task = waitHealthyContainerTask.get()
            container.readyChecks.get().forEach { probe ->
                var probeTask =
                    when (probe) {
                        is LogMessageReadyCheck -> {
                            waitLogMessageTaskFactory
                                .create(
                                    name =
                                        dockerTaskNameGenerator.generate(
                                            container, "waitLogMessage", "Container"),
                                    container = container,
                                    probe = probe,
                                    containerId = startContainerTask.flatMap { it.containerId })
                                .also { it.configure { dependsOn(lastHealthTask) } }
                        }
                        else ->
                            throw IllegalArgumentException(
                                "Unknown readiness probe type: ${probe.javaClass}")
                    }
                lastHealthTask = probeTask.get()
            }

            val stopContainerTask =
                stopContainerTaskFactory
                    .create(
                        name = dockerTaskNameGenerator.generate(container, "stop", "Container"),
                        containerId = createContainerTask.flatMap { it.containerId })
                    .also { it.configure { dependsOn(lastHealthTask) } }

            val removeContainerTask =
                removeContainerTaskFactory
                    .create(
                        name = dockerTaskNameGenerator.generate(container, "remove", "Container"),
                        containerId = createContainerTask.flatMap { it.containerId })
                    .also { it.configure { dependsOn(stopContainerTask) } }

            spec.tasks.get().forEach { task ->
                task.dependsOn(lastHealthTask)
                task.finalizedBy(removeContainerTask.get())
            }
        }
    }
}
