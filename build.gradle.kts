import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10" apply false
    kotlin("kapt") version "1.7.10" apply false

    id("com.diffplug.spotless") version "6.12.0" apply false
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
    id("com.gradle.plugin-publish") version "1.1.0" apply false
    id("org.unbroken-dome.test-sets") version "4.0.0" apply false
}

allprojects {
    repositories {
        mavenCentral()
        maven { url = uri("https://plugins.gradle.org/m2") }
    }

    configurations {
        all {
            resolutionStrategy {
                eachDependency {
                    if (requested.group == "org.jetbrains.kotlin") {
                        useVersion("1.7.10")
                    }
                }
            }
        }
    }

    afterEvaluate {
        if (plugins.hasPlugin(JavaPlugin::class)) {
            apply<JacocoPlugin>()

            configure<JavaPluginExtension> {
                toolchain {
                    languageVersion.set(JavaLanguageVersion.of(11))
                }
            }

            dependencies {
                val testImplementation: Configuration by configurations.getting

                testImplementation("io.kotest:kotest-assertions-core:5.5.4")
                testImplementation("io.kotest:kotest-property:5.5.4")
                testImplementation("io.kotest:kotest-runner-junit5:5.5.4")
            }

            tasks.withType<Test> {
                useJUnitPlatform()
                testLogging {
                    events(STARTED, PASSED, FAILED)
                    showExceptions = true
                    showStackTraces = true
                    showCauses = true
                    exceptionFormat = FULL
                }
            }

            tasks.withType<JacocoReport> {
                reports {
                    with(xml) {
                        required.set(true)
                    }
                    with(html) {
                        required.set(false)
                    }
                }
                setDependsOn(tasks.withType<Test>())
            }
        }

        if (plugins.hasPlugin(KotlinPluginWrapper::class)) {
            tasks.withType<KotlinCompile> {
                kotlinOptions {
                    jvmTarget = "11"
                    freeCompilerArgs = listOf("-Xjsr305=strict")
                }
            }

            configure<SourceSetContainer> {
                afterEvaluate {
                    all {
                        java {
                            // https://arturdryomov.dev/posts/kotlin-code-organization/
                            srcDirs("src/${this@all.name}/kotlinExtensions")
                        }
                    }
                }
            }
        }
    }
}
