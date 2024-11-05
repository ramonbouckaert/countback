package io.bouckaert.countback

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Paths

class JvmFileLoader: FileLoader {
    override suspend fun loadFile(path: String): String {
        return withContext(Dispatchers.IO) {
            Files.readString(Paths.get(path))
        }
    }
}
