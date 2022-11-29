package io.github.meiblorn.requiredocker.task

import com.bmuschko.gradle.docker.tasks.container.DockerExistingContainer
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.model.Frame
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.property
import java.io.Closeable
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

open class DockerWaitLogMessageTask : DockerExistingContainer() {

    @Input
    val logMessage: Property<String> = project.objects.property<String>()

    @Input
    @Optional
    val count: Property<Int> = project.objects.property<Int>()
        .convention(1)

    @Input
    @Optional
    var timeout: Duration = Duration.ofSeconds(60)

    init {
        group = "docker"
        description = "Waits for Docker container to log message"
    }

    override fun runRemoteCommand() {
        logger.quiet("Waiting for container {} to log message: {}", containerId.get(), logMessage.get())

        dockerClient.logContainerCmd(containerId.get()).run {
            withTailAll()
            withFollowStream(true)
            withStdOut(true)
            withStdErr(true)

            val serverStarted = CountDownLatch(count.get())
            exec(object : ResultCallback<Frame> {
                override fun close() {
                }

                override fun onStart(p0: Closeable) {
                }

                override fun onNext(p0: Frame?) {
                    if (p0.toString().contains(logMessage.get())) {
                        serverStarted.countDown()
                    }
                }

                override fun onError(p0: Throwable?) {
                }

                override fun onComplete() {
                }
            })
            try {
                serverStarted.await(timeout.toMillis(), TimeUnit.MILLISECONDS)
            } catch (e: InterruptedException) {
                throw TimeoutException(
                    "Container $containerId did not log message after " +
                            "${timeout.toMillis()} ms: ${logMessage.get()}"
                )
            }
        }

        logger.quiet("Container {} logged message: {}", containerId.get(), logMessage.get())
    }

    fun logMessage(logMessage: String) {
        this.logMessage.set(logMessage)
    }

    fun logMessage(logMessage: Provider<String>) {
        this.logMessage.set(logMessage)
    }

    fun count(count: Int) {
        this.count.set(count)
    }

    fun count(count: Provider<Int>) {
        this.count.set(count)
    }
}
