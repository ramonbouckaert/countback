package io.bouckaert.countback

import io.bouckaert.countback.store.BallotStore

class Election(
    val numberOfVacancies: Int,
    val candidates: Set<Candidate>,
    val ballotStore: BallotStore,
    val roundCountToInt: Boolean = false
) {

    suspend fun performCount(
        excludedCandidates: Set<Candidate> = emptySet(),
        verbose: Boolean = false,
        writeOutput: (String, newParagraph: Boolean) -> Unit = { _, _ -> }
    ): Results {
        val quota = ((ballotStore.getSize() / (numberOfVacancies + 1.0)) + 1.0).toInt().toDouble()

        if (ballotStore.isEmpty()) throw Error("No votes to count")
        if (verbose) writeOutput("Total number of candidates is ${candidates.size}", true)
        if (verbose) writeOutput("Total number of votes to count is ${ballotStore.getSize()}", false)
        if (verbose) writeOutput("Quota for election is $quota", false)

        var countNumber = 1
        val elected: MutableMap<Candidate, VotePile> = mutableMapOf()

        // Count the first preference votes for each candidate.
        var count: Map<Candidate, VotePile> = VotePile(ballotStore.getAllBallotIds(), countNumber, 1.0).groupByHighestPreference(ballotStore, candidates).filterNotNull()

        // Exclude any candidate already deemed to be excluded from consideration and distribute their votes
        var newCount = count.entries.associate { it.key to it.value }
        count.forEach { (candidate, _) ->
            if (candidate in excludedCandidates) {
                if (verbose) writeOutput("Removing candidate $candidate as they have been manually excluded", true)
                newCount = newCount.removeCandidateAndDistributeRemainingVotes(ballotStore, candidate, quota, countNumber, verbose, writeOutput)
            }
        }
        count = newCount

        // If there are fewer candidates than positions, all are elected by default
        if (count.size < numberOfVacancies) {
            if (verbose) writeOutput("The number of eligible candidates (${count.size}) is fewer than the number of vacancies ($numberOfVacancies), so all candidates are elected", true)
            return Results(ballotStore, count.filterNotNull(), quota)
        }

        // Iterate until all positions are filled
        while (true) {
            if (verbose) writeOutput("Starting count #${countNumber}", true)

            var electedThisLoop = false

            // Elect any candidate with more first preference votes than the quota
            var newCount = count.entries.associate { it.key to it.value }
            if (roundCountToInt) newCount = newCount.mapValues { (_, votePile) -> votePile.roundDownToInt() }
            count.sortedByDescending().forEach { (candidate) ->
                val votePile = newCount[candidate]
                if (votePile != null && votePile.count() >= quota) {
                    if (verbose) writeOutput("Electing candidate $candidate as their count (${votePile.count()}) meets or exceeds the quota ($quota)", false)
                    elected[candidate] = votePile
                    val remainingCandidates = count.filterKeys { it != candidate }.sortedByDescending()
                    if (verbose && remainingCandidates.isNotEmpty()) writeOutput(
                        "Remaining candidates: ${remainingCandidates.map { (candidate, votePile) -> "$candidate (${votePile.count()})" }.joinToString(", ")}",
                        false
                    )

                    // Eliminate candidate and distribute second preferences at transfer value
                    newCount = newCount.removeCandidateAndDistributeRemainingVotes(ballotStore, candidate, quota, countNumber, verbose, writeOutput)

                    // Flag that a candidate has been elected this round
                    electedThisLoop = true
                }
            }
            count = newCount

            // Check if all positions have been filled
            if (elected.size == numberOfVacancies) {
                if (verbose) writeOutput("All positions have been filled, count has finished", false)
                return Results(ballotStore, elected, quota)
            }

            // If the number of remaining candidates can fill all the open vacancies, elect them all
            if (count.filterNotNull().size <= (numberOfVacancies - elected.size)) {
                if (verbose) writeOutput("The number of remaining candidates is less than or equal to the number of open vacancies. Electing all remaining candidates", false)
                elected.putAll(count.filterNotNull())
                return Results(ballotStore, elected, quota)
            }

            // If nobody has been elected this round, exclude the candidate with the fewest votes
            if (!electedThisLoop) {
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

                    val remainingCandidates = count.filterKeys { it != excludedCandidate }.sortedByDescending()
                    if (verbose && remainingCandidates.isNotEmpty()) writeOutput(
                        "Remaining candidates: ${remainingCandidates.map { (candidate, votePile) -> "$candidate (${votePile.count()})" }.joinToString(", ")}",
                        false
                    )

                    count = count.removeCandidateAndDistributeRemainingVotes(ballotStore, excludedCandidate, quota, countNumber, verbose, writeOutput)
                }
            }

            countNumber++
        }
    }

    data class Results(
        val ballotStore: BallotStore,
        val winnersAndVotes: Map<Candidate, VotePile>,
        val quota: Double
    )
}