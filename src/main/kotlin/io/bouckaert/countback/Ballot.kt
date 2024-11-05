package io.bouckaert.countback


value class Ballot(val ranking: Array<Candidate>) {
    override fun toString(): String = "(${ranking.joinToString(",") { it.toString().subSequence(0,3).toString() }})"
}