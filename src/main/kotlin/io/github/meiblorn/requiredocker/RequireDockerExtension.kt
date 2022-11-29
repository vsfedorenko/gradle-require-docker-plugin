package io.github.meiblorn.requiredocker

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.domainObjectContainer
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

open class RequireDockerExtension
@Inject constructor(
    objectFactory: ObjectFactory
) {
    val specs: NamedDomainObjectContainer<RequireDockerSpec> =
        objectFactory.domainObjectContainer(
            RequireDockerSpec::class
        ) { objectFactory.newInstance(RequireDockerSpec::class, it) }

    val containers: NamedDomainObjectContainer<RequireDockerContainer> =
        objectFactory.domainObjectContainer(
            RequireDockerContainer::class
        ) { objectFactory.newInstance(RequireDockerContainer::class, it) }

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

    fun container(name: String): NamedDomainObjectProvider<RequireDockerContainer> {
        return containers.named(name)
    }

    fun container(name: String, init: RequireDockerContainer.() -> Unit): RequireDockerContainer {
        return containers.create(name).apply {
            init()
        }
    }
}
