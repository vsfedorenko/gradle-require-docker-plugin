package io.github.meiblorn.requiredocker

internal interface DockerTaskNameGenerator {

    fun generate(container: RequireDockerContainer, action: String, subject: String): String
}

internal open class DockerTaskNameGeneratorImpl : DockerTaskNameGenerator {

    override fun generate(
        container: RequireDockerContainer,
        action: String,
        subject: String
    ): String =
        with(container) {
            return "${spec.name}${action.capitalize()}${name.capitalize()}${subject.capitalize()}"
        }
}
