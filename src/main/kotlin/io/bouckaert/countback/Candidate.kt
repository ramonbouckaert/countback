package io.bouckaert.countback

import io.ktor.util.collections.*
import kotlinx.serialization.Serializable

@Serializable
value class Candidate(val id: Int) {
    constructor(electorateCode: Int, partyCode: Int, candidateCode: Int): this(
        (electorateCode shl 20) or (partyCode shl 10) or candidateCode
    )
    constructor(electorateCode: Int, partyCode: Int, candidateCode: Int, name: String): this(electorateCode, partyCode, candidateCode) {
        if (candidateMap.containsKey(this.id)) {
            if (candidateMap[this.id] != name) {
                throw IllegalStateException("Candidate with ID ${this.id} already exists with name ${candidateMap[this.id]} - clashed name is $name")
            }
        } else candidateMap[this.id] = name
    }
    companion object {
        val candidateMap = ConcurrentMap<Int, String>()
        fun clearCache() {
            candidateMap.clear()
        }
    }
    override fun toString(): String = candidateMap[this.id] ?: throw IllegalStateException("Candidate with ID ${this.id} doesn't exist")

    val electorateCode: Int get() = this.id shr 20
    val partyCode: Int get() = (this.id shr 10) and 0x3ff
    val candidateCode: Int get() = this.id and 0x3ff
}