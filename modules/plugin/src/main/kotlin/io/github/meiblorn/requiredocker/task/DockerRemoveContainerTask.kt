package io.github.meiblorn.requiredocker.task

import com.bmuschko.gradle.docker.tasks.container.DockerRemoveContainer

open class DockerRemoveContainerTask : DockerRemoveContainer() {
    init {
        group = "docker"
        description = "Removes Docker container"
    }
}
