import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "1.1.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "io.github.meiblorn"

repositories {
    mavenCentral()
    maven {
        url = uri("https://plugins.gradle.org/m2")
    }
}

configurations {
    implementation {
        extendsFrom(shadow.get())
    }
}

afterEvaluate {
    with(configurations.shadow.get()) {
        dependencies.remove(project.dependencies.gradleApi())
    }
}

dependencies {
    shadow("com.bmuschko:gradle-docker-plugin:9.0.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val shadowJar by tasks.getting(ShadowJar::class) {
    archiveClassifier.set("")

    configurations = listOf(project.configurations.shadow.get())

    exclude(
        "migrations/*",
        "META-INF/INDEX.LIST",
        "META-INF/*.SF",
        "META-INF/*.DSA",
        "META-INF/*.RSA",
        "META-INF/NOTICE*",
        "META-INF/README*",
        "META-INF/CHANGELOG*",
        "META-INF/DEPENDENCIES*",
        "module-info.class"
    )

    mergeServiceFiles()
}

val jar by tasks.getting(Jar::class) {
    dependsOn(shadowJar)
}

val relocateShadowJar by tasks.creating(ConfigureShadowRelocation::class) {
    target = shadowJar
    prefix = "io.github.meiblorn.requiredocker.shadow"
}

shadowJar.dependsOn(relocateShadowJar)

gradlePlugin {
    plugins.create("requireDocker") {
        id = "io.github.meiblorn.require-docker"
        implementationClass = "io.github.meiblorn.requiredocker.RequireDockerPlugin"
        version = project.version

        displayName = "Require Docker Plugin"
        description = "Gradle plugin to require Docker to be running before executing tasks"
    }
}

pluginBundle {
    website = "https://github.com/meiblorn/gradle-require-docker-plugin"
    vcsUrl = "https://github.com/meiblorn/gradle-require-docker-plugin"

    pluginTags = mapOf(
        "requireDocker" to listOf("docker"),
    )
}
