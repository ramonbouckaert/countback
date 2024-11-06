package io.bouckaert.countback

import kotlinx.coroutines.flow.Flow

interface FileLoader {
    suspend fun loadFile(path: String): Flow<String>

    class FileLoadException(message: String): Exception(message)
}