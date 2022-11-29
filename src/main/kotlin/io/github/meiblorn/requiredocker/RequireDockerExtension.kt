package io.github.meiblorn.requiredocker

import com.bmuschko.gradle.docker.DockerExtension
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.domainObjectContainer
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class RequireDockerExtension
@Inject constructor(
    private val objectFactory: ObjectFactory
) {
    val docker: Property<Docker> = objectFactory.property()

    fun docker(init: Docker.() -> Unit) {
        docker.set(objectFactory.newInstance(Docker::class).apply(init))
    }

    val specs: NamedDomainObjectContainer<RequireDockerSpec> =
        objectFactory.domainObjectContainer(
            RequireDockerSpec::class
        ) { objectFactory.newInstance(RequireDockerSpec::class, it) }

    fun spec(name: String): RequireDockerSpec {
        return specs.named(name).get()
    }

    fun spec(name: String, init: RequireDockerSpec.() -> Unit): RequireDockerSpec {
        return (when (val spec = specs.findByName(name)) {
            null -> specs.create(name)
            else -> spec
        }).apply {
            init()
        }
    }

    class Docker
    @Inject constructor(objectFactory: ObjectFactory) : DockerExtension(objectFactory) {
    }
}
