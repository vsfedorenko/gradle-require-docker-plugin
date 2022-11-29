package io.github.meiblorn.requiredocker

import java.time.Duration

interface RequreDockerReadinessProbe {

    companion object {

        fun logMessage(
            message: String,
            count: Int? = null,
            timeout: Duration? = null
        ): RequreDockerReadinessProbe {
            return LogMessageReadinessProbe(message, count, timeout)
        }
    }
}

class LogMessageReadinessProbe(val message: String, val count: Int? = null, val timeout: Duration? = null) :
    RequreDockerReadinessProbe
