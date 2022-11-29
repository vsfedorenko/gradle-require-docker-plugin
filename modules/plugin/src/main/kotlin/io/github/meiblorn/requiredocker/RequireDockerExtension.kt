package io.github.meiblorn.requiredocker

import com.bmuschko.gradle.docker.DockerExtension
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.domainObjectContainer
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class RequireDockerExtension
@Inject constructor(
    private val objectFactory: ObjectFactory
) {
    var docker: Docker = objectFactory.newInstance()

    fun docker(init: Docker.() -> Unit) {
        docker.init()
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

    open class Docker
    @Inject constructor(objectFactory: ObjectFactory) : DockerExtension(objectFactory) {
    }
}
