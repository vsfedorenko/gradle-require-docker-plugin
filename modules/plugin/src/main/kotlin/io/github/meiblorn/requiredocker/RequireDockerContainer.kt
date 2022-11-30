package io.github.meiblorn.requiredocker

import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property

open class RequireDockerContainer
@Inject
constructor(
    internal val spec: RequireDockerSpec,
    val name: String,
    private val objectFactory: ObjectFactory
) {
    internal val image: Property<String> = objectFactory.property(String::class)

    internal val portBindings: ListProperty<String> =
        objectFactory.listProperty(String::class).convention(mutableListOf())

    internal val envVars: MapProperty<String, String> =
        objectFactory.mapProperty(String::class, String::class).convention(mutableMapOf())

    internal val readyChecks: ListProperty<RequireDockerReadyCheck> =
        objectFactory.listProperty(RequireDockerReadyCheck::class).convention(mutableListOf())

    fun image(value: String) {
        image.set(value)
        image.disallowChanges()
    }

    fun portBindings(vararg value: String) {
        portBindings.addAll(value.toList())
    }

    fun portBindings(value: List<String>) {
        portBindings.addAll(value)
    }

    fun envVars(vararg value: String) {
        if (value.size % 2 != 0) {
            throw IllegalArgumentException("envVars must be a list of key-value pairs")
        }
        envVars.putAll(value.toList().chunked(2).associate { it[0] to it[1] })
    }

    fun envVars(vararg value: Pair<String, String>) {
        envVars.putAll(value.toMap())
    }

    fun envVars(value: Map<String, String>) {
        envVars.putAll(value)
    }

    internal fun finalize() {
        for (property in listOf(image, portBindings, envVars, readyChecks)) {
            property.finalizeValue()
        }
    }
}
