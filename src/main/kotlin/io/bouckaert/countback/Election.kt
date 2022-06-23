package io.bouckaert.countback

import java.io.OutputStream

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
                    throw Error("Candidate $candidate is not in the pool of candidates")
                }
            }
        }
    }

    val quota: Double get() = ((ballots.size / (numberOfVacancies + 1.0)) + 1.0).toInt().toDouble()

    fun performCount(
        excludedCandidates: Set<Candidate> = emptySet(),
        verbose: Boolean = false,
        outputStream: OutputStream = OutputStream.nullOutputStream()
    ): Results {
        if (ballots.isEmpty()) throw Error("No votes to count")
        if (verbose) outputStream.writeln("Total number of candidates is ${candidates.size}")
        if (verbose) outputStream.writeln("Total number of votes to count is ${ballots.size}")
        if (verbose) outputStream.writeln("Quota for election is $quota")

        var countNumber = 1
        val elected: MutableMap<Candidate, VotePile> = mutableMapOf()

        // Count the first preference votes for each candidate.
        var count: Map<Candidate, VotePile> = VotePile(ballots, countNumber, 1.0).groupByHighestPreference(candidates).filterNotNull()

        // Exclude any candidate already deemed to be excluded from consideration and distribute their votes
        var newCount = count.entries.map { it.key to it.value }.toMap()
        count.forEach { (candidate, _) ->
            if (candidate in excludedCandidates) {
                if (verbose) outputStream.writeln("Removing candidate $candidate as they have been manually excluded")
                newCount = newCount.removeCandidateAndDistributeRemainingVotes(candidate, quota, countNumber, verbose, outputStream)
            }
        }
        count = newCount

        // If there are fewer candidates than positions, all are elected by default
        if (count.size < numberOfVacancies) {
            if (verbose) outputStream.writeln("The number of eligible candidates is fewer than the number of vacancies, so all candidates are elected")
            return Results(count.filterNotNull(), quota)
        }

        // Iterate until all positions are filled
        while (true) {
            if (verbose) outputStream.writeln("")
            if (verbose) outputStream.writeln("Starting count #${countNumber}")

            var electedThisLoop = false

            // Elect any candidate with more first preference votes than the quota
            var newCount = count.entries.map { it.key to it.value }.toMap()
            if (roundCountToInt) newCount = newCount.mapValues { (_, votePile) -> votePile.roundDownToInt() }
            count.sortedByDescending().forEach { (candidate) ->
                val votePile = newCount[candidate]
                if (votePile != null && votePile.count() >= quota) {
                    if (verbose) outputStream.writeln("Electing candidate $candidate as their count (${votePile.count()}) meets or exceeds the quota ($quota)")
                    elected[candidate] = votePile

                    // Eliminate candidate and distribute second preferences at transfer value
                    newCount = newCount.removeCandidateAndDistributeRemainingVotes(candidate, quota, countNumber, verbose, outputStream)

                    // Flag that a candidate has been elected this round
                    electedThisLoop = true
                }
            }
            count = newCount

            // Check if all positions have been filled
            if (elected.size == numberOfVacancies) {
                if (verbose) outputStream.writeln("All positions have been filled, count has finished")
                return Results(elected, quota)
            }

            // If the number of remaining candidates can fill all the open vacancies, elect them all
            if (count.filterNotNull().size <= (numberOfVacancies - elected.size)) {
                if (verbose) outputStream.writeln("The number of remaining candidates is less than or equal to the number of open vacancies. Electing all remaining candidates")
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
                    if (verbose) outputStream.writeln("Nobody can be elected this count, so the candidate with the fewest votes is eliminated: $excludedCandidate.")

                    count = count.removeCandidateAndDistributeRemainingVotes(excludedCandidate, quota, countNumber, verbose, outputStream)
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