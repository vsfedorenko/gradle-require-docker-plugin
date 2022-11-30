package io.github.meiblorn.requiredocker

import io.kotest.core.spec.style.DescribeSpec

class SpecCreationFunctionalTest :
    DescribeSpec({
        it("should create spec using native Gradle approach") {
            gradleScope {
                newFile("build.gradle.kts") {
                    """
                    plugins {
                        id("io.github.meiblorn.require-docker")
                    }
                    
                    requireDocker {
                        val main by specs.creating {
                        }
                    }
                    
                    val validateSpecExists by tasks.creating(Task::class) {
                        doLast {
                            requireNotNull(requireDocker.spec("main"))
                        }
                    }
                    """
                        .trimIndent()
                }

                runGradle("validateSpecExists")
            }
        }

        it("should create spec using plugin DSL approach") {
            gradleScope {
                newFile("build.gradle.kts") {
                    """
                    plugins {
                        id("io.github.meiblorn.require-docker")
                    }
                    
                    requireDocker {
                        spec("main") {
                        }
                    }
                    
                    val validateSpecExists by tasks.creating(Task::class) {
                        doLast {
                            requireNotNull(requireDocker.spec("main"))
                        }
                    }
                    """
                        .trimIndent()
                }

                runGradle("build")
            }
        }
    })
