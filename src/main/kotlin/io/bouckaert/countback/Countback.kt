package io.bouckaert.countback

import java.io.OutputStream

class Countback(
    val resigningCandidatePile: VotePile,
    val originalQuota: Double,
    val eligibleCandidates: Set<Candidate>
) {
    
    fun quota(totalVotesAllotted: Double) = (totalVotesAllotted / 2.0) + 1.0

    fun performCount(
        verbose: Boolean = false,
        outputStream: OutputStream = OutputStream.nullOutputStream()
    ): Pair<Candidate, VotePile> {

        val adjustedResults = resigningCandidatePile.adjustFinalCountForCountback(originalQuota)

        var count: Map<Candidate, VotePile> = VotePile(
            adjustedResults.votes.map {
                VotePile.Vote(
                    it.ballot,
                    0,
                    it.transferValue
                )
            }
        ).groupByHighestPreference(eligibleCandidates).filterNotNull()

        var countNumber = 1

        if (count.size < 1) {
            throw Error("No eligible candidates to allocate votes to")
        }

        // Iterate until all positions are filled
        while (true) {
            if (verbose) outputStream.writeln("")
            if (verbose) outputStream.writeln("Starting count #${countNumber}: ${count}")

            // Elect any candidate with more first preference votes than the quota
            count.sortedByDescending().forEach { (candidate) ->
                val votePile = count[candidate]
                if (votePile != null && votePile.count() >= quota(count.intoOnePile().count())) {
                    if (verbose) outputStream.writeln(
                        "Electing candidate $candidate as their count (${votePile.count()}) meets or exceeds the quota (${
                            quota(
                                count.intoOnePile().count()
                            )
                        })"
                    )
                    outputStream.writeln("Elected candidate $candidate ($votePile)")
                    return candidate to votePile
                }
            }

            // If there is only one remaining candidate, elect them
            if (count.filterNotNull().size <= 1) {
                if (verbose) outputStream.writeln("There is only one candidate left, so they will be elected")
                return count.entries.first().let {
                    outputStream.writeln("Elected candidate ${it.key} (${it.value})")
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
                if (verbose) outputStream.writeln("Nobody can be elected this count, so the candidate with the fewest votes is eliminated: $excludedCandidate.")

                count = count.removeCandidateAndDistributeRemainingVotes(
                    excludedCandidate,
                    quota(count.intoOnePile().count()),
                    countNumber,
                    verbose,
                    outputStream
                )
            }

            countNumber++
        }
    }

    private fun VotePile.adjustFinalCountForCountback(quota: Double): VotePile {
        // Split vote pile into before final count and the final count
        val finalCount = this.findFinalCount()
        val beforeFinalCountPile = this.getPileBeforeCount(finalCount)
        val finalCountPile = this.getPileAtCount(finalCount)

        // Distribute final count among eligible candidates
        val finalCountDistributed = finalCountPile.groupByHighestPreference(eligibleCandidates)

        // Calculate NCP, CP and N
        val ncp = finalCountDistributed[null]?.votes?.size?.toDouble() ?: 0.0
        val cp = finalCountDistributed.filterNotNull().intoOnePile().votes.size.toDouble()
        val n = beforeFinalCountPile.count()

        return beforeFinalCountPile.plus(
            finalCountPile.votes.map { vote ->
                VotePile.Vote(
                    vote.ballot,
                    vote.atCount,
                    if (ncp * vote.transferValue >= quota - n) {
                        if (vote.ballot.ranking.firstOrNull { it in eligibleCandidates } == null) {
                            (quota - n) / ncp
                        } else {
                            0.0
                        }
                    } else {
                        if (vote.ballot.ranking.firstOrNull { it in eligibleCandidates } == null) {
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