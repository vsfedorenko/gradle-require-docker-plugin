package io.github.meiblorn.requiredocker

import io.github.meiblorn.requiredocker.task.*
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

internal interface DockerTaskFactory {

    fun newPullImageTask(
        container: RequireDockerContainer,
        init: DockerPullImageTask.() -> Unit = {}
    ): TaskProvider<DockerPullImageTask>

    fun newCreateContainerTask(
        container: RequireDockerContainer,
        init: DockerCreateContainerTask.() -> Unit = {}
    ): TaskProvider<DockerCreateContainerTask>

    fun newStartContainerTask(
        container: RequireDockerContainer,
        containerId: Provider<String>,
        init: DockerStartContainerTask.() -> Unit = {}
    ): TaskProvider<DockerStartContainerTask>

    fun newWaitHealthyContainerTask(
        container: RequireDockerContainer,
        containerId: Provider<String>,
        init: DockerWaitHealthyContainerTask.() -> Unit
    ): TaskProvider<DockerWaitHealthyContainerTask>

    fun newWaitLogMessageTask(
        container: RequireDockerContainer,
        probe: LogMessageReadinessProbe,
        containerId: Provider<String>,
        init: DockerWaitLogMessageTask.() -> Unit
    ): TaskProvider<DockerWaitLogMessageTask>

    fun newStopContainerTask(
        container: RequireDockerContainer,
        containerId: Provider<String>,
        init: DockerStopContainerTask.() -> Unit = {}
    ): TaskProvider<DockerStopContainerTask>

    fun newRemoveContainerTask(
        container: RequireDockerContainer,
        containerId: Provider<String>,
        init: DockerRemoveContainerTask.() -> Unit
    ): TaskProvider<DockerRemoveContainerTask>
}

internal open class DockerTaskFactoryImpl
constructor(
    private val project: Project,
    private val dockerContainerNameGenerator: DockerContainerNameGenerator,
    private val dockerTaskNameGenerator: DockerTaskNameGenerator,
) : DockerTaskFactory {

    override fun newPullImageTask(
        container: RequireDockerContainer,
        init: DockerPullImageTask.() -> Unit
    ): TaskProvider<DockerPullImageTask> {
        val name = dockerTaskNameGenerator.generate(container, "pull", "Image")
        return project.tasks.register<DockerPullImageTask>(name) {
            image.set(container.image)
            init()
        }
    }

    override fun newCreateContainerTask(
        container: RequireDockerContainer,
        init: DockerCreateContainerTask.() -> Unit
    ): TaskProvider<DockerCreateContainerTask> {
        val name = dockerTaskNameGenerator.generate(container, "create", "Container")
        return project.tasks.register<DockerCreateContainerTask>(name) {
            containerName.set(dockerContainerNameGenerator.generate(container))
            imageId.set(container.image)
            with(hostConfig) {
                portBindings.set(container.portBindings)
            }
            envVars.set(container.envVars)
            init()
        }
    }

    override fun newStartContainerTask(
        container: RequireDockerContainer,
        containerId: Provider<String>,
        init: DockerStartContainerTask.() -> Unit
    ): TaskProvider<DockerStartContainerTask> {
        val name = dockerTaskNameGenerator.generate(container, "start", "Container")
        return project.tasks.register<DockerStartContainerTask>(name) {
            this.containerId.set(containerId)
            init()
        }
    }

    override fun newWaitHealthyContainerTask(
        container: RequireDockerContainer,
        containerId: Provider<String>,
        init: DockerWaitHealthyContainerTask.() -> Unit
    ): TaskProvider<DockerWaitHealthyContainerTask> {
        val name = dockerTaskNameGenerator.generate(container, "waitHealthy", "Container")
        return project.tasks.register<DockerWaitHealthyContainerTask>(name) {
            this.containerId.set(containerId)
            init()
        }
    }

    override fun newWaitLogMessageTask(
        container: RequireDockerContainer,
        probe: LogMessageReadinessProbe,
        containerId: Provider<String>,
        init: DockerWaitLogMessageTask.() -> Unit
    ): TaskProvider<DockerWaitLogMessageTask> {
        val name = dockerTaskNameGenerator.generate(container, "waitLogMessage", "Container")
        return project.tasks.register<DockerWaitLogMessageTask>(name) {
            this.containerId.set(containerId)
            this.logMessage.set(probe.message)
            probe.count?.let { this.count.set(it) }
            probe.timeout?.let { this.timeout = it }

            init()
        }
    }

    override fun newStopContainerTask(
        container: RequireDockerContainer,
        containerId: Provider<String>,
        init: DockerStopContainerTask.() -> Unit
    ): TaskProvider<DockerStopContainerTask> {
        val name = dockerTaskNameGenerator.generate(container, "stop", "Container")
        return project.tasks.register<DockerStopContainerTask>(name) {
            this.containerId.set(containerId)
            init()
        }
    }

    override fun newRemoveContainerTask(
        container: RequireDockerContainer,
        containerId: Provider<String>,
        init: DockerRemoveContainerTask.() -> Unit
    ): TaskProvider<DockerRemoveContainerTask> {
        val name = dockerTaskNameGenerator.generate(container, "remove", "Container")
        return project.tasks.register<DockerRemoveContainerTask>(name) {
            this.containerId.set(containerId)
            init()
        }
    }
}
