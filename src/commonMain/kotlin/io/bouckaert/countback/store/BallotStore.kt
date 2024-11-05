package io.bouckaert.countback.store

import io.bouckaert.countback.Ballot
import io.bouckaert.countback.Candidate

interface BallotStore {
    val size: Int
    fun isEmpty(): Boolean = size == 0
    fun getAllBallotIds(): Collection<Int>
    fun getFullBallot(ballotId: Int): Ballot
    fun getHighestRankedCandidateForBallot(ballotId: Int, ofList: Collection<Candidate>? = null): Candidate?
}
