package io.bouckaert.countback

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readLine


class JsFileLoader: FileLoader {
    override suspend fun loadFile(path: String): Sequence<String> {
        val file = Path("../../../../$path")
        if (!SystemFileSystem.exists(file)) throw FileLoader.Companion.FileLoadException("File at \"../../../../$path\" could not be found")
        val buffer = SystemFileSystem.source(file).buffered()

        return sequence {
            while (!buffer.exhausted()) {
                buffer.readLine()?.let { yield(it) } ?: break
            }
        }
    }
}