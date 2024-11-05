package io.bouckaert.countback

interface FileLoader {
    suspend fun loadFile(path: String): Sequence<String>

    companion object {
        class FileLoadException(message: String): Exception(message)
    }
}