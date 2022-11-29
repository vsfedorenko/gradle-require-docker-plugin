package io.github.meiblorn.requiredocker

import com.bmuschko.gradle.docker.DockerRemoteApiPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create

open class RequireDockerPlugin : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        apply<DockerRemoteApiPlugin>()

        val extension = newExtension()

        afterEvaluate {
            extension.specs.forEach { spec ->
                spec.finalize()
                apply(spec)
            }
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

    internal fun Project.newExtension(): RequireDockerExtension {
        return extensions.create("requireDocker", RequireDockerExtension::class)
    }
}
