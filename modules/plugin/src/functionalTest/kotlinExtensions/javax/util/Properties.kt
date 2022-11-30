package javax.util

import java.io.File
import java.io.FileInputStream
import java.util.*

fun Properties.load(file: File): Properties {
    if (!file.exists()) {
        return this
    }

    FileInputStream(file).use { load(it) }
    return this
}
