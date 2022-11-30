package io.github.meiblorn.requiredocker

import java.time.Duration

interface RequireDockerReadyCheck {

    companion object {

        fun logMessage(
            message: String,
            count: Int? = null,
            timeout: Duration? = null
        ): RequireDockerReadyCheck {
            return LogMessageReadyCheck(message, count, timeout)
        }
    }
}

class LogMessageReadyCheck(
    val message: String,
    val count: Int? = null,
    val timeout: Duration? = null
) : RequireDockerReadyCheck
