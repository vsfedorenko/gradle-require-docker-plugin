package io.github.meiblorn.requiredocker

import io.kotest.core.TestConfiguration
import io.kotest.engine.spec.tempdir
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import java.util.*
import javax.io.newDirectory
import javax.util.load

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
            .withDebug(debug)
            .withProjectDir(projectDir)
            .withTestKitDir(projectDir.newDirectory(".testkit"))
            .withPluginClasspath()
            .withJoCoCo()
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

    /**
     * There is no built-it support for code coverage in TestKit.
     * Those tests run in separate JVM and configuration of JaCoCo plugin is not taken into account.
     *
     * @see <a href="https://github.com/koral--/jacoco-gradle-testkit-plugin/blob/master/README.md">Jacoco TestKit Plugin</a>
     */
    private fun GradleRunner.withJoCoCo(): GradleRunner {
        val gradlePropertiesFile = File(projectDir, "gradle.properties")
        val gradleProperties = Properties().load(gradlePropertiesFile)

        val testkitGradleProperties = Properties().apply {
            GradleScope::class.java
                .classLoader
                ?.getResourceAsStream("testkit-gradle.properties")
                ?.use { load(it) }
        }

        gradleProperties.putAll(testkitGradleProperties)
        gradleProperties.store(gradlePropertiesFile.outputStream(), null)
        return this
    }
}

fun <T> TestConfiguration.gradleScope(block: GradleScope.() -> T): T {
    return GradleScope(projectDir = tempdir()).block()
}
