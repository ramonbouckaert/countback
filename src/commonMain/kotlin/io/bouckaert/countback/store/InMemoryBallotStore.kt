package io.bouckaert.countback.store

import io.bouckaert.countback.*
import kotlinx.coroutines.flow.Flow

class InMemoryBallotStore(
    private val preferencesIn: Flow<Preference>
): BallotStore {
    private var ballots: Map<Long, Ballot>? = null
    override suspend fun getSize(): Int = getBallots().size

    override suspend fun getAllBallotIds(): Collection<Long> = getBallots().keys

    override suspend fun getHighestRankedCandidateForBallot(ballotId: Long, ofList: Collection<Candidate>?): Candidate? =
        getFullBallot(ballotId).ranking.firstOrNull { if (ofList == null) true else it in ofList }

    override suspend fun close() {
        ballots = null
    }

    private suspend fun getFullBallot(ballotId: Long): Ballot = getBallots()[ballotId] ?: throw IllegalStateException("Could not find ballot for ID $ballotId")

    private suspend fun getBallots(): Map<Long, Ballot> {
        return ballots ?: run {
            preferencesIn.toBallotsWithIds().also { ballots = it }
        }
    }
}
