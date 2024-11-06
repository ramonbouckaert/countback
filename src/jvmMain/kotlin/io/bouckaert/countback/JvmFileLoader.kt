package io.bouckaert.countback

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.withContext
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.exists

class JvmFileLoader: FileLoader {
    override suspend fun loadFile(path: String): Flow<String> {
        val modifiedPath = "build/processedResources/js/main/$path"
        val file = Paths.get(modifiedPath)
        if (!file.exists()) throw FileLoader.FileLoadException("File at \"$modifiedPath\" could not be found")
        return withContext(Dispatchers.IO) {
            Files.newBufferedReader(file, Charset.forName("utf8")).lineSequence().asFlow()
        }
    }
}
