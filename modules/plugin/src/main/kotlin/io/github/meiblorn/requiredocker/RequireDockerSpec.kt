package io.github.meiblorn.requiredocker

import javax.inject.Inject
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Task
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.kotlin.dsl.domainObjectContainer
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.newInstance

open class RequireDockerSpec
@Inject
constructor(val name: String, private val objectFactory: ObjectFactory) {
    val containers: NamedDomainObjectContainer<RequireDockerContainer> =
        objectFactory.domainObjectContainer(
            RequireDockerContainer::class,
            { objectFactory.newInstance(RequireDockerContainer::class, this, it) })

    val tasks: ListProperty<Task> =
        objectFactory.listProperty(Task::class).convention(mutableListOf())

    fun container(name: String): RequireDockerContainer {
        return containers.named(name).get()
    }

    fun container(name: String, init: RequireDockerContainer.() -> Unit): RequireDockerContainer {
        return (when (val container = containers.findByName(name)) {
                null -> containers.create(name)
                else -> container
            })
            .apply { init() }
    }

    fun requiredBy(vararg task: Task) {
        this.tasks.addAll(task.toList())
    }

    internal fun finalize() {
        for (property in listOf(tasks)) {
            property.finalizeValue()
        }
        containers.forEach { it.finalize() }
    }
}
