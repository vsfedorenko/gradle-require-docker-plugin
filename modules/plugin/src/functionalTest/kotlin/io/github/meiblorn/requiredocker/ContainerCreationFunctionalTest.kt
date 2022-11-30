package io.github.meiblorn.requiredocker

import io.kotest.core.spec.style.DescribeSpec

class ContainerCreationFunctionalTest :
    DescribeSpec({
        it("should create container using native Gradle approach") {
            gradleScope {
                newFile("build.gradle.kts") {
                    """
                    plugins {
                        id("io.github.meiblorn.require-docker")
                    }
                    
                    requireDocker {
                        val main by specs.creating {
                            val postgres by containers.creating {
                                image("postgres:latest")
                            }
                        }
                    }
                    
                    val validateContainerExists by tasks.creating(Task::class) {
                        doLast {
                            requireNotNull(requireDocker.spec("main").container("postgres"))
                        }
                    }
                    """
                        .trimIndent()
                }

                runGradle("validateContainerExists")
            }
        }

        it("should create container using plugin DSL approach") {
            gradleScope {
                newFile("build.gradle.kts") {
                    """
                    plugins {
                        id("io.github.meiblorn.require-docker")
                    }
                    
                    requireDocker {
                        spec("main") {
                            container("postgres") {
                                image("postgres:latest")
                            }
                        }
                    }
                    
                    val validateContainerExists by tasks.creating(Task::class) {
                        doLast {
                            requireNotNull(requireDocker.spec("main").container("postgres"))
                        }
                    }
                    """
                        .trimIndent()
                }

                runGradle("validateContainerExists")
            }
        }
    })
