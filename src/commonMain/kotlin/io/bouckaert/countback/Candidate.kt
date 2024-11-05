package io.bouckaert.countback

import co.touchlab.stately.collections.ConcurrentMutableMap
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class Candidate(val id: Int) {
    constructor(electorateCode: Int, partyCode: Int, candidateCode: Int): this(
        (electorateCode shl 12) or (partyCode shl 6) or candidateCode
    ) {
        if (electorateCode > 0x3f) throw IllegalArgumentException("Cannot construct a candidate with an electorate code of $electorateCode as it is larger than the maximum of ${0x3f}")
        if (partyCode > 0x3f) throw IllegalArgumentException("Cannot construct a candidate with a party code of $partyCode as it is larger than the maximum of ${0x3f}")
        if (candidateCode > 0x3f) throw IllegalArgumentException("Cannot construct a candidate with a candidate code of $candidateCode as it is larger than the maximum of ${0x3f}")
    }
    constructor(electorateCode: Int, partyCode: Int, candidateCode: Int, name: String): this(electorateCode, partyCode, candidateCode) {
        if (candidateMap.containsKey(this.id)) {
            if (candidateMap[this.id] != name) {
                throw IllegalStateException("Candidate with ID ${this.id} already exists with name ${candidateMap[this.id]} - clashed name is $name")
            }
        } else candidateMap[this.id] = name
    }
    companion object {
        val candidateMap = ConcurrentMutableMap<Int, String>()
        fun clearCache() {
            candidateMap.clear()
        }
    }
    override fun toString(): String = candidateMap[this.id] ?: throw IllegalStateException("Candidate with ID ${this.id} doesn't exist")

    val electorateCode: Int get() = this.id shr 12
    val partyCode: Int get() = (this.id shr 6) and 0x3f
    val candidateCode: Int get() = this.id and 0x3f
}