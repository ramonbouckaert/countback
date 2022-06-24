package io.bouckaert.countback

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.InputStream
import java.util.zip.ZipInputStream

class ACTDataLoader(
    val getStream: () -> InputStream?,
) {
    private val csvReader = csvReader()

    fun loadElectorates() = readFromZip("Electorates.txt")
        .associateBy({ it["ecode"]?.toInt() }) { it["electorate"] }
        .filterNotNull()

    fun loadCandidates(electorateMap: Map<Int, String>) =
        readFromZip("Candidates.txt")
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

    fun loadBallots(electorate: String, candidateMapping: Map<Pair<Int, Int>, Candidate>) =
        readFromZip("${electorate}Total.txt")
            .groupBy({ "${it["batch"]}${it["pindex"]}" }) { entry ->
                val pcode = entry["pcode"]?.toInt() ?: throw Error("Can't convert ${entry["pcode"]} to Int")
                val ccode = entry["ccode"]?.toInt() ?: throw Error("Can't convert ${entry["ccode"]} to Int")
                entry["pref"]?.toInt() to candidateMapping[pcode to ccode]
            }
            .mapValues { entry ->
                linkedSetOf(*entry.value.sortedBy { it.first }.mapNotNull { it.second }.toTypedArray())
            }
            .mapNotNull { Ballot(it.value) }

    private fun readFromZip(filename: String): List<Map<String, String>> {
        val stream = getStream() ?: throw Error("Could not read input stream")
        var result: List<Map<String, String>>? = null

        ZipInputStream(stream).use { zis ->
            var zipEntry = zis.nextEntry

            while (zipEntry != null) {
                if (zipEntry.name == filename) {
                    result = csvReader.readAllWithHeader(zis)
                    break
                } else {
                    zis.closeEntry()
                }
                zipEntry = zis.nextEntry
            }
        }

        return result ?: throw Error("Could not retrieve file $filename")
    }
}