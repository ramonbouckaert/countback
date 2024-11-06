package io.bouckaert.countback

import io.bouckaert.countback.store.BallotStore

class Countback(
    val ballotStore: BallotStore,
    val resigningCandidatePile: VotePile,
    val originalQuota: Double,
    val eligibleCandidates: Set<Candidate>
) {
    
    fun quota(totalVotesAllotted: Double) = (totalVotesAllotted / 2.0) + 1.0

    suspend fun performCount(
        verbose: Boolean = false,
        writeOutput: (String, newParagraph: Boolean) -> Unit = { _, _ -> }
    ): Pair<Candidate, VotePile> {

        val adjustedResults = resigningCandidatePile.adjustFinalCountForCountback(originalQuota)

        var count: Map<Candidate, VotePile> = VotePile(
            adjustedResults.votes.map {
                VotePile.Vote(
                    it.ballotId,
                    0,
                    it.transferValue
                )
            }
        ).groupByHighestPreference(ballotStore, eligibleCandidates).filterNotNull()

        var countNumber = 1

        if (count.isEmpty()) {
            throw Error("No eligible candidates to allocate votes to")
        }

        // Iterate until all positions are filled
        while (true) {
            if (verbose) writeOutput("Starting count #$countNumber", true)

            // Elect any candidate with more first preference votes than the quota
            count.sortedByDescending().forEach { (candidate) ->
                val votePile = count[candidate]
                if (votePile != null && votePile.count() >= quota(count.intoOnePile().count())) {
                    if (verbose) writeOutput(
                        "Electing candidate $candidate as their count (${votePile.count()}) meets or exceeds the quota (${
                            quota(
                                count.intoOnePile().count()
                            )
                        })",
                        false
                    )
                    writeOutput("Elected candidate $candidate ($votePile)", true)
                    return candidate to votePile
                }
            }

            // If there is only one remaining candidate, elect them
            if (count.filterNotNull().size <= 1) {
                if (verbose) writeOutput("There is only one candidate left, so they will be elected", false)
                return count.entries.first().let {
                    writeOutput("Elected candidate ${it.key} (${it.value})", true)
                    it.key to it.value
                }
            }

            // If we haven't been able to elect the winner, we need to exclude the candidate with the fewest votes
            val excludedCandidate = count.entries.fold(null as Map.Entry<Candidate?, VotePile>?) { acc, entry ->
                if (acc == null) {
                    entry
                } else {
                    if (entry.value.count() < acc.value.count()) {
                        entry
                    } else {
                        acc
                    }
                }
            }?.key
            if (excludedCandidate != null) {
                if (verbose) writeOutput("Nobody can be elected this count, so the candidate with the fewest votes is eliminated: $excludedCandidate.", false)

                count = count.removeCandidateAndDistributeRemainingVotes(
                    ballotStore,
                    excludedCandidate,
                    quota(count.intoOnePile().count()),
                    countNumber,
                    verbose,
                    writeOutput
                )
            }

            countNumber++
        }
    }

    private suspend fun VotePile.adjustFinalCountForCountback(quota: Double): VotePile {
        // Split vote pile into before final count and the final count
        val finalCount = this.findFinalCount()
        val beforeFinalCountPile = this.getPileBeforeCount(finalCount)
        val finalCountPile = this.getPileAtCount(finalCount)

        // Distribute final count among eligible candidates
        val finalCountDistributed = finalCountPile.groupByHighestPreference(ballotStore, eligibleCandidates)

        // Calculate NCP, CP and N
        val ncp = finalCountDistributed[null]?.votes?.size?.toDouble() ?: 0.0
        val cp = finalCountDistributed.filterNotNull().intoOnePile().votes.size.toDouble()
        val n = beforeFinalCountPile.count()

        return beforeFinalCountPile.plus(
            finalCountPile.votes.map { vote ->
                VotePile.Vote(
                    vote.ballotId,
                    vote.atCount,
                    if (ncp * vote.transferValue >= quota - n) {
                        if (ballotStore.getHighestRankedCandidateForBallot(vote.ballotId, eligibleCandidates) == null) {
                            (quota - n) / ncp
                        } else {
                            0.0
                        }
                    } else {
                        if (ballotStore.getHighestRankedCandidateForBallot(vote.ballotId, eligibleCandidates) == null) {
                            vote.transferValue
                        } else {
                            (quota - n - (ncp * vote.transferValue)) / cp
                        }
                    }
                )
            }.let(::VotePile)
        )
    }

    private fun VotePile.findFinalCount() = this.votes.fold(1) { acc, vote -> maxOf(acc, vote.atCount) }
}