package io.github.meiblorn.requiredocker.task

import com.bmuschko.gradle.docker.tasks.container.DockerStopContainer
import io.github.meiblorn.requiredocker.RequireDockerTask
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

open class StopContainerTask : DockerStopContainer(), RequireDockerTask {

    init {
        group = "docker"
        description = "Stops Docker container"
    }
}

interface StopContainerTaskFactory {

    fun create(name: String, containerId: Provider<String>): TaskProvider<StopContainerTask>
}

open class StopContainerTaskFactoryImpl @Inject constructor(private val project: Project) :
    StopContainerTaskFactory {

    override fun create(
        name: String,
        containerId: Provider<String>
    ): TaskProvider<StopContainerTask> {
        return project.tasks.register<StopContainerTask>(name) { this.containerId.set(containerId) }
    }
}
