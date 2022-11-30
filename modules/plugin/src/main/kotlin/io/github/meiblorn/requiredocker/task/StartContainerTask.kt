package io.github.meiblorn.requiredocker.task

import com.bmuschko.gradle.docker.tasks.container.DockerStartContainer
import io.github.meiblorn.requiredocker.RequireDockerTask
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

open class StartContainerTask : DockerStartContainer(), RequireDockerTask {

    init {
        group = "docker"
        description = "Starts Docker container"
    }
}

interface StartContainerTaskFactory {

    fun create(name: String, containerId: Provider<String>): TaskProvider<StartContainerTask>
}

open class StartContainerTaskFactoryImpl @Inject constructor(private val project: Project) :
    StartContainerTaskFactory {

    override fun create(
        name: String,
        containerId: Provider<String>
    ): TaskProvider<StartContainerTask> {
        return project.tasks.register<StartContainerTask>(name) {
            this.containerId.set(containerId)
        }
    }
}
