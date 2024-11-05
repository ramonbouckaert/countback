package io.bouckaert.countback

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader

class ACTDataLoader(
    private val basePath: String,
    private val fileLoader: FileLoader
) {

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