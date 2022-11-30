package io.github.meiblorn.requiredocker.task

import com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer
import io.github.meiblorn.requiredocker.RequireDockerContainer
import io.github.meiblorn.requiredocker.RequireDockerTask
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

open class CreateContainerTask @Inject constructor(objectFactory: ObjectFactory) :
    DockerCreateContainer(objectFactory), RequireDockerTask {

    init {
        group = "docker"
        description = "Creates Docker container"
    }
}

interface CreateContainerTaskFactory {

    fun create(name: String, container: RequireDockerContainer): TaskProvider<CreateContainerTask>
}

open class CreateContainerTaskFactoryImpl @Inject constructor(private val project: Project) :
    CreateContainerTaskFactory {

    override fun create(
        name: String,
        container: RequireDockerContainer
    ): TaskProvider<CreateContainerTask> {
        return project.tasks.register<CreateContainerTask>(name) {
            imageId.set(container.image)
            with(hostConfig) { portBindings.set(container.portBindings) }
            envVars.set(container.envVars)
        }
    }
}
