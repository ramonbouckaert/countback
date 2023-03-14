package io.bouckaert.countback

import kotlin.collections.LinkedHashSet

value class Ballot(val ranking: LinkedHashSet<Candidate>) {
    override fun toString(): String = "(${ranking.joinToString(",") { it.name.subSequence(0,3).toString() }})"
}