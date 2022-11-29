package io.github.meiblorn.requiredocker.task

import com.bmuschko.gradle.docker.tasks.container.DockerStopContainer

open class DockerStopContainerTask : DockerStopContainer() {
    init {
        group = "docker"
        description = "Stops Docker container"
    }
}
