package io.bouckaert.countback

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

class ACTDataLoader(
    private val basePath: String,
    private val fileLoader: FileLoader = object : FileLoader {
        private val client = HttpClient(Js)
        override suspend fun loadFile(path: String): String =
            client.get(path).bodyAsText()
    }
) {
    interface FileLoader {
        suspend fun loadFile(path: String): String
    }

    private val csvReader = csvReader()

    suspend fun loadElectorates() = readFromPath("${basePath}Electorates.txt")
        .associateBy({ it["ecode"]?.toInt() }) { it["electorate"] }
        .filterNotNull()

    suspend fun loadCandidates(electorateMap: Map<Int, String>) =
        readFromPath("${basePath}Candidates.txt")
            .groupBy { it["ecode"]?.toInt() }
            .mapValues {
                it.value.associateBy({ entry ->
                    val pcode = entry["pcode"]?.toInt()
                    val ccode = entry["ccode"]?.toInt()
                    if (pcode != null && ccode != null) {
                        pcode to ccode
                    } else null
                }) { entry ->
                    entry["cname"]?.let(::Candidate)
                }.filterNotNull()
            }.mapKeys { it.key?.let(electorateMap::get) }
            .filterNotNull()

    suspend fun loadBallots(electorate: String, candidateMapping: Map<Pair<Int, Int>, Candidate>) =
        readFromPath("${basePath}${electorate}Total.txt")
            .let {
                println("starting groupBy")
                it
            }
            .groupBy({ "${it["batch"]}${it["pindex"]}" }) { entry ->
                val pcode = entry["pcode"]?.toInt() ?: throw Error("Can't convert ${entry["pcode"]} to Int")
                val ccode = entry["ccode"]?.toInt() ?: throw Error("Can't convert ${entry["ccode"]} to Int")
                entry["pref"]?.toInt() to candidateMapping[pcode to ccode]
            }
            .let {
                println("starting mapValues")
                it
            }
            .mapValues { entry ->
                linkedSetOf(*entry.value.sortedBy { it.first }.mapNotNull { it.second }.toTypedArray())
            }
            .let {
                println("starting mapNotNull")
                it
            }
            .mapNotNull { Ballot(it.value) }

    private suspend fun readFromPath(path: String): List<Map<String, String>> =
        fileLoader.loadFile(path).let(csvReader::readAllWithHeader)
}