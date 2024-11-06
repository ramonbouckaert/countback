package io.bouckaert.countback.store

import io.bouckaert.countback.Candidate

interface BallotStore {
    suspend fun getSize(): Int
    suspend fun isEmpty(): Boolean = getSize() == 0
    suspend fun getAllBallotIds(): Collection<Long>
    suspend fun getHighestRankedCandidateForBallot(ballotId: Long, ofList: Collection<Candidate>? = null): Candidate?
    suspend fun close()
}
