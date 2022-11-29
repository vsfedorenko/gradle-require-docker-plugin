package io.github.meiblorn.requiredocker

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.gradle.testkit.runner.TaskOutcome

class RequireLifecycleFunctionalTest : DescribeSpec({

    it("should wrap task on require") {
        val buildResult = gradleScope {
            newFile("build.gradle.kts") {
                """
                    plugins {
                        id("io.github.meiblorn.require-docker")
                    }
                    
                    requireDocker {
                        val main by specs.creating {
                            val postgres by containers.creating {
                                image("postgres:alpine")
                                envVars("POSTGRES_PASSWORD", "password")
                            }
                        }
                    }
                    
                    val sampleTask by tasks.creating(Task::class) {
                        requireDocker.spec("main").requiredBy(this)
                    }
                    """.trimIndent()
            }

            runGradle("sampleTask")
        }

        with(buildResult) {
            tasks.map { it.path }
                .shouldContainInOrder(
                    listOf(
                        ":mainPullPostgresImage",
                        ":mainCreatePostgresContainer",
                        ":mainStartPostgresContainer",
                        ":mainWaitHealthyPostgresContainer",
                        ":sampleTask",
                        ":mainStopPostgresContainer",
                        ":mainRemovePostgresContainer",
                    )
                )

            tasks.map { it.outcome }
                .all { it == TaskOutcome.SUCCESS }
                .shouldBe(true)
        }
    }

    it("should remove container on task failure") {
        val buildResult = gradleScope {
            newFile("build.gradle.kts") {
                """
                    plugins {
                        id("io.github.meiblorn.require-docker")
                    }
                    
                    requireDocker {
                        val main by specs.creating {
                            val postgres by containers.creating {
                                image("postgres:alpine")
                                envVars("POSTGRES_PASSWORD", "password")
                            }
                        }
                    }
                    
                    val sampleTask by tasks.creating(Task::class) {
                        doLast {
                            throw RuntimeException("Sample exception")
                        }
                        requireDocker.spec("main").requiredBy(this)
                    }
                    """.trimIndent()
            }

            runGradle("sampleTask", expectFailure = true)
        }

        with(buildResult) {
            tasks.map { it.path }
                .shouldContainInOrder(
                    listOf(
                        ":mainPullPostgresImage",
                        ":mainCreatePostgresContainer",
                        ":mainStartPostgresContainer",
                        ":mainWaitHealthyPostgresContainer",
                        ":sampleTask",
                        ":mainStopPostgresContainer",
                        ":mainRemovePostgresContainer",
                    )
                )

            val sampleTask = tasks
                .find { it.path == ":sampleTask" }
                .shouldNotBeNull()

            tasks.filter { it != sampleTask }
                .map { it.outcome }
                .all { it == TaskOutcome.SUCCESS }
                .shouldBe(true)

            sampleTask.outcome shouldBe TaskOutcome.FAILED
        }
    }

})
