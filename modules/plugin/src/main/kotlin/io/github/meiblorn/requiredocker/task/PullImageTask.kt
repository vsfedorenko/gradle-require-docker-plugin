package io.github.meiblorn.requiredocker.task

import com.bmuschko.gradle.docker.tasks.image.DockerPullImage
import io.github.meiblorn.requiredocker.RequireDockerContainer
import io.github.meiblorn.requiredocker.RequireDockerTask
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

open class PullImageTask @Inject constructor(private val objectFactory: ObjectFactory) :
    DockerPullImage(), RequireDockerTask {

    init {
        group = "docker"
        description = "Pulls Docker image"
    }
}

interface PullImageTaskFactory {

    fun create(name: String, container: RequireDockerContainer): TaskProvider<PullImageTask>
}

open class PullImageTaskFactoryImpl @Inject constructor(private val project: Project) :
    PullImageTaskFactory {

    override fun create(
        name: String,
        container: RequireDockerContainer
    ): TaskProvider<PullImageTask> {
        return project.tasks.register<PullImageTask>(name) { image.set(container.image) }
    }
}
