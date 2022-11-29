package io.github.meiblorn.requiredocker.task

import com.bmuschko.gradle.docker.tasks.image.DockerPullImage

open class DockerPullImageTask : DockerPullImage() {
    init {
        group = "docker"
        description = "Pulls Docker image"
    }
}
