package io.bouckaert.countback

data class Preference(
    val ballotId: Long,
    val candidate: Candidate,
    val rank: Int
)
