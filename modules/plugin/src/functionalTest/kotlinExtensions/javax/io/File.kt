package javax.io

import java.io.File
import java.nio.file.Files

fun File.newDirectory(prefix: String): File {
    val tempDirectory = Files.createTempDirectory(this.toPath(), prefix)
    return tempDirectory.toFile()
}
