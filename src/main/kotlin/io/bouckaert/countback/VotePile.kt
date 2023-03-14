package io.bouckaert.countback

import kotlin.math.min

class VotePile(
    val votes: List<Vote> = emptyList()
) {
    constructor(ballots: List<Ballot>, atCount: Int, transferValue: Double): this(
        ballots.map { Vote(it, atCount, transferValue) }
    )

    fun count(transferValue: Double = 1.0): Double = votes.fold(0.0) { acc, vote -> acc + min(vote.transferValue, transferValue) }

    fun plus(inputVotes: VotePile, atCount: Int? = null, transferValue: Double? = null): VotePile {
        return VotePile(
            votes.plus(inputVotes.votes.map {
                Vote(
                    it.ballot,
                    atCount ?: it.atCount,
                    min(it.transferValue, transferValue ?: 1.0))
            })
        )
    }

    fun minus(subtractVotes: VotePile) = VotePile(
        votes.minus(subtractVotes.votes.toSet())
    )

    fun groupByHighestPreference(
        ofCandidates: Collection<Candidate?>
    ): Map<Candidate?, VotePile> =
        if (ofCandidates.isEmpty()) {
            mapOf(null to this)
        } else votes
            .groupBy { vote -> vote.ballot.ranking.firstOrNull { it in ofCandidates.filterNotNull() } }
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
                it.ballot,
                it.atCount,
                it.transferValue * adjustmentFraction
            )
        })
    }

    class Vote(
        val ballot: Ballot,
        val atCount: Int = 0,
        val transferValue: Double = 1.0,
    ) {
        override fun toString(): String = "${ballot}@$transferValue"
    }

    override fun toString(): String = "${votes.size} ballots worth ${count().toFloat()} votes"

    fun toString(transferValue: Double): String = "${votes.size} ballots worth ${count(transferValue).toFloat()} votes"
}