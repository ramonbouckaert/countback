package io.bouckaert.countback

import java.io.OutputStream

fun <K, V> Map<out K?, V?>.filterNotNull(): Map<K, V> = this.mapNotNull {
    it.key?.let { key ->
        it.value?.let { value ->
            key to value
        }
    }
}.toMap()

fun <C> Map<out C, VotePile>.intoOnePile(): VotePile = this.values.fold(VotePile()) { a, v -> a.plus(v) }

fun Collection<VotePile>.intoOnePile(): VotePile = this.fold(VotePile()) { a, v -> a.plus(v) }

fun Map<Candidate, VotePile>.removeCandidateAndDistributeRemainingVotes(
    candidateToRemove: Candidate,
    quota: Double,
    countNumber: Int = 1,
    verbose: Boolean = false,
    outputStream: OutputStream = OutputStream.nullOutputStream()
): Map<Candidate, VotePile> {
    val votesInCandidatesPile = this[candidateToRemove] ?: VotePile()

    return if (votesInCandidatesPile.count() >= quota) {
        // If the candidate reached a quota before being excluded, they won, so we only distribute the votes received at the point the quota was met, at a calculated transfer value

        // Split candidate's votes into votes received before the quota was met, and votes received at the count the quota was met
        val countAtWhichQuotaWasMet = votesInCandidatesPile.findCountAtWhichQuotaWasMet(quota)
        val votesReceivedWhenQuotaMet = votesInCandidatesPile.getPileAtCount(countAtWhichQuotaWasMet)
        val votesReceivedBeforeQuotaMet = votesInCandidatesPile.getPileBeforeCount(countAtWhichQuotaWasMet)

        // Distribute full votes received when the quota was met to their next preferred candidate still in the race
        val redistributedVotes = votesReceivedWhenQuotaMet.getFullVotesOnly().groupByHighestPreference(this.keys.minus(candidateToRemove))

        // Calculate the vote value of the surplus (numerator for transfer value)
        val voteValueOfSurplus = (votesReceivedBeforeQuotaMet.count() + votesReceivedWhenQuotaMet.getFullVotesOnly().count()) - quota

        // Calculate the number of non-exhausted ballot papers received at the time the quota was met (denominator for transfer value)
        val nonExhaustedBallotPapers = votesReceivedWhenQuotaMet.getFullVotesOnly().votes.size - (redistributedVotes[null]?.getFullVotesOnly()?.votes?.size ?: 0)

        // Compute transfer value
        val transferValue = maxOf(minOf(voteValueOfSurplus / nonExhaustedBallotPapers, 1.0), 0.0)
        if (verbose) outputStream.writeln("Transfer value calculated at ${transferValue.toFloat()}")

        // Pass on the redistributed vote map and the transfer value to the next part of the function
        redistributedVotes to transferValue
    } else {
        // If the candidate did not reach a quota before being excluded, they didn't win, so we distribute all their votes at their present transfer value
        votesInCandidatesPile.groupByHighestPreference(this.keys.minus(candidateToRemove)) to 1.0
    }.let { (redistributedVotes, transferValue) ->
        if (verbose) outputStream.writeln("${redistributedVotes[null]?.toString(transferValue)} have been exhausted.")
        if (verbose) outputStream.writeln("Distributing ${redistributedVotes.filterNotNull().intoOnePile().toString(transferValue)} to remaining candidates...")
        this.filter { (candidate, _) -> candidate != candidateToRemove }
            .mapValues { (candidate, votePile) -> redistributedVotes[candidate]?.let {
                votePile.plus(it, countNumber, transferValue)
            } ?: votePile }
    }
}

fun Map<Candidate, VotePile>.sortedByDescending() = this.entries.sortedByDescending { (_, votePile) -> votePile.count() }.map { it.key to it.value }.toMap()

fun OutputStream.writeln(string: String) {
    this.write(string.toByteArray())
    this.write("\r\n".toByteArray())
}