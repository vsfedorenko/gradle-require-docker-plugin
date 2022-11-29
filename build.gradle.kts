import com.diffplug.gradle.spotless.SpotlessExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`

    id("com.diffplug.spotless") version "6.11.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("com.gradle.plugin-publish") version "1.1.0"
}

group = "io.github.meiblorn"

description =
    """Gradle plugin for requiring Docker containers 
    to be up and running before executing attached tasks
    """.trimIndent()

repositories {
    mavenCentral()
    maven { url = uri("https://plugins.gradle.org/m2") }
}

configurations {
    implementation { extendsFrom(shadow.get()) }
}

afterEvaluate {
    with(configurations.shadow.get()) {
        dependencies.remove(project.dependencies.gradleApi())
    }
}

dependencies {
    shadow("com.bmuschko:gradle-docker-plugin:9.0.1")
}

configure<SourceSetContainer> {
    main {
        java {
            // https://arturdryomov.dev/posts/kotlin-code-organization/
            srcDirs("src/main/kotlinExtensions")
        }
    }
    test {
        java {
            // https://arturdryomov.dev/posts/kotlin-code-organization/
            srcDirs("src/test/kotlinExtensions")
        }
    }
}

configure<JavaPluginExtension> {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
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
    val requireDocker by
    plugins.creating {
        id = "io.github.meiblorn.require-docker"
        implementationClass =
            "io.github.meiblorn.requiredocker.RequireDockerPlugin"
        version = project.version

        displayName = "Require Docker Plugin"
        description =
            """Gradle plugin to require Docker to be up 
                and running before executing attached tasks
            """.trimIndent()
    }
}

pluginBundle {
    website = "https://github.com/meiblorn/gradle-require-docker-plugin"
    vcsUrl = "https://github.com/meiblorn/gradle-require-docker-plugin"
    tags = listOf("docker", "require", "gradle", "plugin")
}

configure<SpotlessExtension> {
    kotlin {
        target("src/main/kotlin/**/*.kt")
        target("src/main/kotlinExtensions/**/*.kt")

        ktfmt().dropboxStyle().configure {
            it.setMaxWidth(120)
            it.setBlockIndent(4)
            it.setContinuationIndent(4)
            it.setRemoveUnusedImport(true)
        }
    }
    kotlinGradle {
        ktlint()
    }
}
