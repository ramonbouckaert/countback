package io.bouckaert.countback

import kotlin.jvm.JvmInline


@JvmInline
value class Ballot(val ranking: Array<Candidate>) {
    override fun toString(): String = "(${ranking.joinToString(",") { it.toString().subSequence(0,3).toString() }})"
}