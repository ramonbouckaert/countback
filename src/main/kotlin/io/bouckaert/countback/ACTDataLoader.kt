package io.bouckaert.countback

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File
import java.io.InputStream

class ACTDataLoader(
    val electoratesFile: File? = null,
    val candidatesFile: File? = null,
    val votesFile: File? = null,
    val electoratesStream: InputStream? = null,
    val candidatesStream: InputStream? = null,
    val votesStream: InputStream? = null,
    ) {
    private val csvReader = csvReader()

    fun loadElectorates() = readFile(electoratesFile, electoratesStream)
        .associateBy({ it["ecode"]?.toInt() }) { it["electorate"] }
        .filterNotNull()

    fun loadCandidates(electorateMap: Map<Int, String>) =
        readFile(candidatesFile, candidatesStream)
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

    fun loadBallots(candidateMapping: Map<Pair<Int, Int>, Candidate>) =
        readFile(votesFile, votesStream)
            .groupBy({ "${it["batch"]}${it["pindex"]}" }) { entry ->
                val pcode = entry["pcode"]?.toInt() ?: throw Error("Can't convert ${entry["pcode"]} to Int")
                val ccode = entry["ccode"]?.toInt() ?: throw Error("Can't convert ${entry["ccode"]} to Int")
                entry["pref"]?.toInt() to candidateMapping[pcode to ccode]
            }
            .mapValues { entry -> linkedSetOf(*entry.value.sortedBy { it.first }.mapNotNull { it.second }.toTypedArray()) }
            .mapNotNull { Ballot(it.value) }

    private fun readFile(file: File?, stream: InputStream?): List<Map<String, String>> {
        if (file != null) {
            return csvReader.readAllWithHeader(file)
        }
        if (stream != null) {
            return csvReader.readAllWithHeader(stream)
        }
        throw Error("File and stream are both null")
    }
}