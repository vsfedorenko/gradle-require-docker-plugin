package io.github.meiblorn.requiredocker.task

import com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

open class DockerCreateContainerTask
@Inject constructor(objectFactory: ObjectFactory) : DockerCreateContainer(objectFactory) {

    init {
        group = "docker"
        description = "Creates Docker container"
    }
}
