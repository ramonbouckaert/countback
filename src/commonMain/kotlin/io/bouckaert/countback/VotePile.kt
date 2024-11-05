package io.bouckaert.countback

import io.bouckaert.countback.store.BallotStore
import kotlin.math.min

class VotePile(
    val votes: Collection<Vote> = emptyList()
) {
    constructor(ballotIds: Collection<Int>, atCount: Int, transferValue: Double): this(
        ballotIds.map { Vote(it, atCount, transferValue) }
    )

    fun count(transferValue: Double = 1.0): Double = votes.fold(0.0) { acc, vote -> acc + min(vote.transferValue, transferValue) }

    fun plus(inputVotes: VotePile, atCount: Int? = null, transferValue: Double? = null): VotePile {
        return VotePile(
            votes.plus(inputVotes.votes.map {
                Vote(
                    it.ballotId,
                    atCount ?: it.atCount,
                    min(it.transferValue, transferValue ?: 1.0))
            })
        )
    }

    fun minus(subtractVotes: VotePile) = VotePile(
        votes.minus(subtractVotes.votes.toSet())
    )

    fun groupByHighestPreference(
        store: BallotStore,
        ofCandidates: Collection<Candidate?>
    ): Map<Candidate?, VotePile> =
        if (ofCandidates.isEmpty()) {
            mapOf(null to this)
        } else votes
            .groupBy { vote -> store.getHighestRankedCandidateForBallot(vote.ballotId, ofCandidates.filterNotNull()) }
            .mapValues { VotePile(it.value) }
            .let { if (!it.containsKey(null)) it.plus(null to VotePile()) else it }

    fun getPileAtCount(
        countNumber: Int
    ): VotePile =
        VotePile(
            this.votes.filter { vote -> vote.atCount == countNumber }
        )

    fun getPileBeforeCount(
        countNumber: Int
    ): VotePile =
        VotePile(
            this.votes.filter { vote -> vote.atCount < countNumber }
        )

    fun findCountAtWhichQuotaWasMet(quota: Double): Int {
        var runningCount = 0.0
        return this.votes.sortedBy { it.atCount }.fold(1) { acc, vote ->
            runningCount += vote.transferValue
            if (runningCount <= quota) {
                vote.atCount
            } else acc
        }
    }

    fun getFullVotesOnly() = VotePile(this.votes.filter { vote -> vote.transferValue == 1.0 })

    fun roundDownToInt(): VotePile {
        val adjustmentFraction = this.count().let { it.toInt().toDouble() / it }
        return VotePile(this.votes.map {
            Vote(
                it.ballotId,
                it.atCount,
                it.transferValue * adjustmentFraction
            )
        })
    }

    class Vote(
        val ballotId: Int,
        val atCount: Int = 0,
        val transferValue: Double = 1.0,
    ) {
        override fun toString(): String = "${ballotId}@$transferValue"
        fun toString(store: BallotStore) = "${store.getFullBallot(ballotId)}@$transferValue"
    }

    override fun toString(): String = "${votes.size} ballots worth ${count().toFloat()} votes"

    fun toString(transferValue: Double): String = "${votes.size} ballots worth ${count(transferValue).toFloat()} votes"
}