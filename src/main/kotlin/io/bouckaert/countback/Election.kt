package io.bouckaert.countback

class Election(
    val numberOfVacancies: Int,
    val candidates: Set<Candidate>,
    val ballots: List<Ballot>,
    val roundCountToInt: Boolean = false
) {
    init {
        ballots.forEach { vote ->
            vote.ranking.forEach { candidate ->
                if (candidate !in candidates) {
                    throw Error("io.bouckaert.countback.Candidate $candidate is not in the pool of candidates")
                }
            }
        }
    }

    val quota: Double get() = ((ballots.size / (numberOfVacancies + 1.0)) + 1.0).toInt().toDouble()

    fun performCount(
        excludedCandidates: Set<Candidate> = emptySet(),
        verbose: Boolean = false,
        writeOutput: (String, newParagraph: Boolean) -> Unit = { _, _ -> }
    ): Results {
        if (ballots.isEmpty()) throw Error("No votes to count")
        if (verbose) writeOutput("Total number of candidates is ${candidates.size}", true)
        if (verbose) writeOutput("Total number of votes to count is ${ballots.size}", false)
        if (verbose) writeOutput("Quota for election is $quota", false)

        var countNumber = 1
        val elected: MutableMap<Candidate, VotePile> = mutableMapOf()

        // Count the first preference votes for each candidate.
        var count: Map<Candidate, VotePile> = VotePile(ballots, countNumber, 1.0).groupByHighestPreference(candidates).filterNotNull()

        // Exclude any candidate already deemed to be excluded from consideration and distribute their votes
        var newCount = count.entries.map { it.key to it.value }.toMap()
        count.forEach { (candidate, _) ->
            if (candidate in excludedCandidates) {
                if (verbose) writeOutput("Removing candidate $candidate as they have been manually excluded", true)
                newCount = newCount.removeCandidateAndDistributeRemainingVotes(candidate, quota, countNumber, verbose, writeOutput)
            }
        }
        count = newCount

        // If there are fewer candidates than positions, all are elected by default
        if (count.size < numberOfVacancies) {
            if (verbose) writeOutput("The number of eligible candidates is fewer than the number of vacancies, so all candidates are elected", true)
            return Results(count.filterNotNull(), quota)
        }

        // Iterate until all positions are filled
        while (true) {
            if (verbose) writeOutput("Starting count #${countNumber}", true)

            var electedThisLoop = false

            // Elect any candidate with more first preference votes than the quota
            var newCount = count.entries.map { it.key to it.value }.toMap()
            if (roundCountToInt) newCount = newCount.mapValues { (_, votePile) -> votePile.roundDownToInt() }
            count.sortedByDescending().forEach { (candidate) ->
                val votePile = newCount[candidate]
                if (votePile != null && votePile.count() >= quota) {
                    if (verbose) writeOutput("Electing candidate $candidate as their count (${votePile.count()}) meets or exceeds the quota ($quota)", false)
                    elected[candidate] = votePile

                    // Eliminate candidate and distribute second preferences at transfer value
                    newCount = newCount.removeCandidateAndDistributeRemainingVotes(candidate, quota, countNumber, verbose, writeOutput)

                    // Flag that a candidate has been elected this round
                    electedThisLoop = true
                }
            }
            count = newCount

            // Check if all positions have been filled
            if (elected.size == numberOfVacancies) {
                if (verbose) writeOutput("All positions have been filled, count has finished", false)
                return Results(elected, quota)
            }

            // If the number of remaining candidates can fill all the open vacancies, elect them all
            if (count.filterNotNull().size <= (numberOfVacancies - elected.size)) {
                if (verbose) writeOutput("The number of remaining candidates is less than or equal to the number of open vacancies. Electing all remaining candidates", false)
                elected.putAll(count.filterNotNull())
                return Results(elected, quota)
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

                    count = count.removeCandidateAndDistributeRemainingVotes(excludedCandidate, quota, countNumber, verbose, writeOutput)
                }
            }

            countNumber++
        }
    }

    data class Results(
        val winnersAndVotes: Map<Candidate, VotePile>,
        val quota: Double
    )
}