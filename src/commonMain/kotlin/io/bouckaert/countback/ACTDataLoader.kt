package io.bouckaert.countback

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

import kotlinx.coroutines.flow.*

class ACTDataLoader(
    private val basePath: String,
    private val fileLoader: FileLoader
) {

    suspend fun loadElectorates(): Map<Int, String> = readFromPath("${basePath}Electorates.txt")
        .mapNotNull {
            val ecode = it["ecode"]?.toInt() ?: return@mapNotNull null
            val electorate = it["electorate"] ?: return@mapNotNull null
            ecode to electorate
        }
        .toList()
        .toMap()

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

    suspend fun loadPreferences(electorate: String, ecode: Int): Flow<Preference> =
        readFromPath("${basePath}${electorate}Total.txt")
            .map { entry ->
                val batch = entry["batch"]?.toLong() ?: throw Error("Can't convert ${entry["batch"]} to Long")
                val pindex = entry["pindex"]?.toLong() ?: throw Error("Can't convert ${entry["pindex"]} to Long")
                val pcode = entry["pcode"]?.toInt() ?: throw Error("Can't convert ${entry["pcode"]} to Int")
                val ccode = entry["ccode"]?.toInt() ?: throw Error("Can't convert ${entry["ccode"]} to Int")
                val pref = entry["pref"]?.toInt() ?: throw Error("Can't convert ${entry["pref"]} to Int")

                if (batch > 0xFFFFFFFFF) throw IllegalArgumentException("Cannot construct a ballot ID with a batch code of $batch as it is larger than the maximum of ${0xFFFFFFFFF}")
                if (pindex > 0xFFFFFFFFF) throw IllegalArgumentException("Cannot construct a ballot ID with a pindex code of $pindex as it is larger than the maximum of ${0xFFFFFFFFF}")
                val ballotId = (batch shl 36) or pindex

                Preference(
                    ballotId,
                    Candidate(ecode, pcode, ccode),
                    pref
                )
            }

    private suspend fun readFromPath(path: String): Flow<Map<String, String>> {
        val flow = fileLoader.loadFile(path)
        var header: List<String>? = null
        return flow.mapNotNull { line ->
            if (header == null) {
                header = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()).map { it.trim('"') }
                return@mapNotNull null
            }
            line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()).mapIndexed { index, element -> header!![index] to element.trim('"') }.toMap()
        }
    }
}