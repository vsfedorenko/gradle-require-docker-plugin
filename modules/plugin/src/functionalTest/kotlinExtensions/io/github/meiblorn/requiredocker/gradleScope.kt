package io.github.meiblorn.requiredocker

import io.kotest.core.TestConfiguration
import io.kotest.engine.spec.tempdir
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File

class GradleScope(var projectDir: File) {

    private var debug: Boolean = false

    fun enableDebug() {
        debug = true
    }

    fun newFile(fileName: String, bodySupplier: () -> String = { "" }): File {
        return File(projectDir, fileName)
            .also { it.parentFile.mkdirs() }
            .also { it.writeText(bodySupplier()) }
    }

    fun runGradle(vararg arguments: String, expectFailure: Boolean? = false): BuildResult {
        val mergedArguments =
            (setOf("--stacktrace", "--info") + arguments).toTypedArray()

        return GradleRunner
            .create()
            .let { if (debug) it.withDebug(true) else it }
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .forwardOutput()
            .withArguments(*mergedArguments)
            .let {
                if (expectFailure == true) {
                    it.buildAndFail()
                } else {
                    it.build()
                }
            }
    }
}

fun <T> TestConfiguration.gradleScope(block: GradleScope.() -> T): T {
    return GradleScope(projectDir = tempdir()).block()
}
