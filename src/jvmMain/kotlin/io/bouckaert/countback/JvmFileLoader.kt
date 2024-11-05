package io.bouckaert.countback

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

class JvmFileLoader: FileLoader {
    override suspend fun loadFile(path: String): Sequence<String> {
        return withContext(Dispatchers.IO) {
            Files.newBufferedReader(Paths.get(path), Charset.forName("utf8")).lineSequence()
        }
    }
}
