package io.github.meiblorn.requiredocker

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class RequireDockerContainer
@Inject constructor(
    internal val spec: RequireDockerSpec,
    val name: String,
    private val objectFactory: ObjectFactory
) {
    internal val image: Property<String> = objectFactory.property(String::class)

    internal val portBindings: ListProperty<String> =
        objectFactory.listProperty(String::class)
            .convention(mutableListOf())

    internal val envVars: MapProperty<String, String> =
        objectFactory.mapProperty(String::class, String::class)
            .convention(mutableMapOf())

    internal val readinessProbes: ListProperty<RequreDockerReadinessProbe> =
        objectFactory.listProperty(RequreDockerReadinessProbe::class)
            .convention(mutableListOf())

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

    fun envVars(vararg value: Pair<String, String>) {
        envVars.putAll(value.toMap())
    }

    fun envVars(value: Map<String, String>) {
        envVars.putAll(value)
    }

    fun readinessProbe(vararg value: RequreDockerReadinessProbe) {
        readinessProbes.addAll(value.toList())
    }

    internal fun finalize() {
        for (property in listOf(image, portBindings, envVars, readinessProbes)) {
            property.finalizeValue()
        }
    }
}
