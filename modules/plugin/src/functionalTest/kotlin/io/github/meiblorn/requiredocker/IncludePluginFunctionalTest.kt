package io.github.meiblorn.requiredocker

import io.kotest.core.spec.style.DescribeSpec

class IncludePluginFunctionalTest : DescribeSpec({

    it("should run with plugin applied") {
        gradleScope {
            newFile("build.gradle.kts") {
                """
                    plugins {
                        id("io.github.meiblorn.require-docker")
                    }
                    
                    val validatePluginApplied by tasks.creating(Task::class) {
                        doLast {
                            requireNotNull(project.plugins.findPlugin("io.github.meiblorn.require-docker"))
                        }
                    }
                    
                    val validateExtensionApplied by tasks.creating(Task::class) {
                        doLast {
                            requireNotNull(project.extensions.findByName("requireDocker"))
                        }
                    }
                    """.trimIndent()
            }

            runGradle("validatePluginApplied", "validateExtensionApplied")
        }
    }

    it("should run with extension called") {
        gradleScope {
            newFile("build.gradle.kts") {
                """
                    plugins {
                        id("io.github.meiblorn.require-docker")
                    }
                    
                    requireDocker {
                    }
                    
                    val validatePluginApplied by tasks.creating(Task::class) {
                        doLast {
                            requireNotNull(project.plugins.findPlugin("io.github.meiblorn.require-docker"))
                        }
                    }
                    
                    val validateExtensionApplied by tasks.creating(Task::class) {
                        doLast {
                            requireNotNull(project.extensions.findByName("requireDocker"))
                        }
                    }
                    """.trimIndent()
            }

            runGradle("validatePluginApplied", "validateExtensionApplied")
        }
    }
})
