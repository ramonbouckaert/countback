package io.bouckaert.countback

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.exists

class JvmFileLoader: FileLoader {
    override suspend fun loadFile(path: String): Sequence<String> {
        val file = Paths.get(path)
        if (!file.exists()) throw FileLoader.Companion.FileLoadException("File at \"$path\" could not be found")
        return withContext(Dispatchers.IO) {
            Files.newBufferedReader(file, Charset.forName("utf8")).lineSequence()
        }
    }
}
