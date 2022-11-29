package io.github.meiblorn.requiredocker.task


import com.bmuschko.gradle.docker.tasks.container.DockerExistingContainer
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import java.time.Duration
import java.util.concurrent.TimeoutException

open class DockerWaitHealthyContainerTask : DockerExistingContainer() {

    @Input
    @Optional
    val timeout: Duration = Duration.ofSeconds(60)

    @Input
    @Optional
    val checkInterval: Duration = Duration.ofSeconds(5)

    init {
        group = "docker"
        description = "Waits for Docker container to become healthy"
    }

    override fun runRemoteCommand() {
        logger.quiet("Waiting for container {} to be healthy", containerId.get())

        val endTime = System.currentTimeMillis() + timeout.toMillis()
        val sleepTime = checkInterval.toMillis()

        while (System.currentTimeMillis() < endTime) {
            if (isHealthy()) {
                break;
            }

            Thread.sleep(sleepTime)
        }

        if (!isHealthy()) {
            throw TimeoutException(
                "Container $containerId is not healthy after ${timeout.toMillis()} milliseconds"
            )
        }

        logger.quiet("Container {} is healthy", containerId.get())
    }

    private fun isHealthy(): Boolean {
        val command = dockerClient.inspectContainerCmd(containerId.get())
        val response = command.exec()
        val state = response?.state

        val health = state?.health
        if (health != null) {
            return health.status == "healthy"
        }

        return state?.status == "running"
    }
}
