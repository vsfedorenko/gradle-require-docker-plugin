package io.github.meiblorn.requiredocker.task

import com.bmuschko.gradle.docker.tasks.container.DockerExistingContainer
import io.github.meiblorn.requiredocker.RequireDockerTask
import java.time.Duration
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

open class WaitHealthyContainerTask : DockerExistingContainer(), RequireDockerTask {

    @field:Input @field:Optional var timeout: Duration = Duration.ofSeconds(60)

    @field:Input @field:Optional var checkInterval: Duration = Duration.ofSeconds(2)

    @field:Input var healthyThreshold: Int = 3

    init {
        group = "docker"
        description = "Waits for Docker container to become healthy"
    }

    override fun runRemoteCommand() {
        logger.quiet("Waiting for container {} to be healthy", containerId.get())

        val endTime = System.currentTimeMillis() + timeout.toMillis()
        val sleepTime = checkInterval.toMillis()

        var healthyCount = 0
        while (System.currentTimeMillis() < endTime) {
            if (isHealthy() && healthyCount++ >= healthyThreshold) {
                break
            }

            Thread.sleep(sleepTime)
        }

        if (!isHealthy()) {
            throw TimeoutException(
                "Container ${containerId.get()} is not healthy after ${timeout.toMillis()} milliseconds")
        }

        logger.quiet("Container {} is healthy", containerId.get())
    }

    private fun isHealthy(): Boolean {
        logger.info("Inspecting container {}", containerId.get())

        val command = dockerClient.inspectContainerCmd(containerId.get())
        val response = command.exec()
        logger.debug("Container inspected: {}", response)

        val state = response?.state

        val health = state?.health
        if (health != null) {
            return health.status == "healthy"
        }

        return state?.status == "running"
    }
}

interface WaitHealthyContainerTaskFactory {

    fun create(name: String, containerId: Provider<String>): TaskProvider<WaitHealthyContainerTask>
}

open class WaitHealthyContainerTaskFactoryImpl @Inject constructor(private val project: Project) :
    WaitHealthyContainerTaskFactory {

    override fun create(
        name: String,
        containerId: Provider<String>
    ): TaskProvider<WaitHealthyContainerTask> {
        return project.tasks.register<WaitHealthyContainerTask>(name) {
            this.containerId.set(containerId)
        }
    }
}
