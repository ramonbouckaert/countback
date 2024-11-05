package io.bouckaert.countback

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class ACTDataLoader(
    private val basePath: String,
    private val fileLoader: FileLoader = object : FileLoader {
        private val client = HttpClient(Js)
        override suspend fun loadFile(path: String): String {
            val result = client.get(path)
            if (result.status === HttpStatusCode.NotFound) throw FileLoadException("File $path could not be found")
            return client.get(path).bodyAsText()
        }
    }
) {
    companion object {
        class FileLoadException(message: String): Exception(message)
    }
    interface FileLoader {
        suspend fun loadFile(path: String): String
    }

    private val csvReader = csvReader()

    suspend fun loadElectorates(): Map<Int, String> = readFromPath("${basePath}Electorates.txt")
        .associateBy({ it["ecode"]?.toInt() }) { it["electorate"] }
        .filterNotNull()

    suspend fun loadCandidates(): Set<Candidate> {
        Candidate.clearCache()
        return readFromPath("${basePath}Candidates.txt")
            .mapNotNull { entry ->
                val ecode = entry["ecode"]?.toInt() ?: return@mapNotNull null
                val pcode = entry["pcode"]?.toInt() ?: return@mapNotNull null
                val ccode = entry["ccode"]?.toInt() ?: return@mapNotNull null
                val cname = entry["cname"] ?: return@mapNotNull null
                val c = Candidate(ecode, pcode, ccode, cname)
                c
            }.toSet()
    }

    suspend fun loadBallots(electorate: String, ecode: Int): Collection<Ballot> =
        readFromPath("${basePath}${electorate}Total.txt")
            .groupBy({ "${it["batch"]}${it["pindex"]}" }) { entry ->
                val pcode = entry["pcode"]?.toInt() ?: throw Error("Can't convert ${entry["pcode"]} to Int")
                val ccode = entry["ccode"]?.toInt() ?: throw Error("Can't convert ${entry["ccode"]} to Int")
                entry["pref"]?.toInt() to Candidate(ecode, pcode, ccode)
            }
            .mapValues { entry ->
                entry.value.sortedBy { it.first }.map { it.second }.toTypedArray()
            }
            .mapNotNull { Ballot(it.value) }

    private suspend fun readFromPath(path: String): List<Map<String, String>> =
        fileLoader.loadFile(path).let(csvReader::readAllWithHeader)
}