package io.github.meiblorn.requiredocker.task

import com.bmuschko.gradle.docker.tasks.container.DockerStartContainer

open class DockerStartContainerTask : DockerStartContainer() {
    init {
        group = "docker"
        description = "Starts Docker container"
    }
}
