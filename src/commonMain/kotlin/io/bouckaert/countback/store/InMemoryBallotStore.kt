package io.bouckaert.countback.store

import io.bouckaert.countback.Ballot
import io.bouckaert.countback.Candidate

class InMemoryBallotStore(
    ballotsIn: Sequence<Ballot>
): BallotStore {
    private val ballots = ballotsIn.mapIndexed { index: Int, ballot: Ballot -> index to ballot }.toMap()
    override val size: Int = ballots.size

    override fun getAllBallotIds(): Collection<Int> = ballots.keys

    override fun getFullBallot(ballotId: Int): Ballot = ballots[ballotId] ?: throw IllegalStateException("Could not find ballot for ID $ballotId")

    override fun getHighestRankedCandidateForBallot(ballotId: Int, ofList: Collection<Candidate>?): Candidate? =
        getFullBallot(ballotId).ranking.firstOrNull { if (ofList == null) true else it in ofList }
}
