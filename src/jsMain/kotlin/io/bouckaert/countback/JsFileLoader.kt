package io.bouckaert.countback

import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class JsFileLoader : FileLoader {
    private val client = HttpClient(Js)
    override suspend fun loadFile(path: String): Flow<String> {
        return flow {
            client.prepareGet(path).execute { response ->
                if (response.status != HttpStatusCode.OK) throw FileLoader.FileLoadException("Cannot load file ${response.request.url}, request responded with ${response.status}")
                val channel = response.bodyAsChannel()
                do {
                    val line = channel.readUTF8Line()?.let { emit(it) }
                } while (line !== null)
            }
        }
    }
}