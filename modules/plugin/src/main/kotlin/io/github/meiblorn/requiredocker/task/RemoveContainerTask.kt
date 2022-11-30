package io.github.meiblorn.requiredocker.task

import com.bmuschko.gradle.docker.tasks.container.DockerRemoveContainer
import io.github.meiblorn.requiredocker.RequireDockerTask
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

open class RemoveContainerTask @Inject constructor(private val objectFactory: ObjectFactory) :
    DockerRemoveContainer(), RequireDockerTask {

    init {
        group = "docker"
        description = "Removes Docker container"
    }
}

interface RemoveContainerTaskFactory {

    fun create(name: String, containerId: Provider<String>): TaskProvider<RemoveContainerTask>
}

open class RemoveContainerTaskFactoryImpl @Inject constructor(private val project: Project) :
    RemoveContainerTaskFactory {

    override fun create(
        name: String,
        containerId: Provider<String>
    ): TaskProvider<RemoveContainerTask> {
        return project.tasks.register<RemoveContainerTask>(name) {
            this.containerId.set(containerId)
        }
    }
}
