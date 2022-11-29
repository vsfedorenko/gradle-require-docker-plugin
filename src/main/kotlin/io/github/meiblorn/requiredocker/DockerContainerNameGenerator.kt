package io.github.meiblorn.requiredocker

import org.gradle.api.Project

internal interface DockerContainerNameGenerator {

    fun generate(container: RequireDockerContainer): String
}

open class DockerContainerNameGeneratorImpl(private val project: Project) : DockerContainerNameGenerator {

    override fun generate(container: RequireDockerContainer): String = with(container) {
        return "gradle-${project.name}-${spec.name}-${name}-${System.currentTimeMillis()}"
    }
}
