package io.bouckaert.countback

import io.bouckaert.countback.store.BallotStore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*

fun <K, V> Map<out K?, V?>.filterNotNull(): Map<K, V> = this.mapNotNull {
    it.key?.let { key ->
        it.value?.let { value ->
            key to value
        }
    }
}.toMap()

fun <C> Map<out C, VotePile>.intoOnePile(): VotePile = this.values.fold(VotePile()) { a, v -> a.plus(v) }

fun Collection<VotePile>.intoOnePile(): VotePile = this.fold(VotePile()) { a, v -> a.plus(v) }

suspend fun Map<Candidate, VotePile>.removeCandidateAndDistributeRemainingVotes(
    ballotStore: BallotStore,
    candidateToRemove: Candidate,
    quota: Double,
    countNumber: Int = 1,
    verbose: Boolean = false,
    writeOutput: (String, newParagraph: Boolean) -> Unit
): Map<Candidate, VotePile> {
    val votesInCandidatesPile = this[candidateToRemove] ?: VotePile()

    return if (votesInCandidatesPile.count() >= quota) {
        // If the candidate reached a quota before being excluded, they won, so we only distribute the votes received at the point the quota was met, at a calculated transfer value

        // Split candidate's votes into votes received before the quota was met, and votes received at the count the quota was met
        val countAtWhichQuotaWasMet = votesInCandidatesPile.findCountAtWhichQuotaWasMet(quota)
        val votesReceivedWhenQuotaMet = votesInCandidatesPile.getPileAtCount(countAtWhichQuotaWasMet)
        val votesReceivedBeforeQuotaMet = votesInCandidatesPile.getPileBeforeCount(countAtWhichQuotaWasMet)

        // Distribute full votes received when the quota was met to their next preferred candidate still in the race
        val redistributedVotes = votesReceivedWhenQuotaMet.getFullVotesOnly().groupByHighestPreference(ballotStore, this.keys.minus(candidateToRemove))

        // Calculate the vote value of the surplus (numerator for transfer value)
        val voteValueOfSurplus = (votesReceivedBeforeQuotaMet.count() + votesReceivedWhenQuotaMet.getFullVotesOnly().count()) - quota

        // Calculate the number of non-exhausted ballot papers received at the time the quota was met (denominator for transfer value)
        val nonExhaustedBallotPapers = votesReceivedWhenQuotaMet.getFullVotesOnly().votes.size - (redistributedVotes[null]?.getFullVotesOnly()?.votes?.size ?: 0)

        // Compute transfer value
        val transferValue = maxOf(minOf(voteValueOfSurplus / nonExhaustedBallotPapers, 1.0), 0.0)
        if (verbose) writeOutput("Transfer value calculated at ${transferValue.toFloat()}", true)

        // Pass on the redistributed vote map and the transfer value to the next part of the function
        redistributedVotes to transferValue
    } else {
        // If the candidate did not reach a quota before being excluded, they didn't win, so we distribute all their votes at their present transfer value
        votesInCandidatesPile.groupByHighestPreference(ballotStore, this.keys.minus(candidateToRemove)) to 1.0
    }.let { (redistributedVotes, transferValue) ->
        if (verbose) writeOutput("${redistributedVotes[null]?.toString(transferValue)} have been exhausted.", false)
        if (verbose) writeOutput("Distributing ${redistributedVotes.filterNotNull().intoOnePile().toString(transferValue)} to remaining candidates...", false)
        this.filter { (candidate, _) -> candidate != candidateToRemove }
            .mapValues { (candidate, votePile) -> redistributedVotes[candidate]?.let {
                votePile.plus(it, countNumber, transferValue)
            } ?: votePile }
    }
}

fun Map<Candidate, VotePile>.sortedByDescending() = this.entries.sortedByDescending { (_, votePile) -> votePile.count() }.map { it.key to it.value }.toMap()

suspend inline fun Flow<Preference>.toBallotsWithIds(): Map<Long, Ballot> = this
    .groupToList { it.ballotId }
    .map { preferences -> preferences.first to Ballot(preferences.second.map { it.candidate }.toTypedArray()) }
    .toMap()

inline fun <T, K> Flow<T>.groupToList(crossinline getKey: (T) -> K): Flow<Pair<K, List<T>>> = flow {
    val storage = mutableMapOf<K, MutableList<T>>()
    collect { t -> storage.getOrPut(getKey(t)) { mutableListOf() } += t }
    storage.forEach { (k, ts) -> emit(k to ts) }
}

suspend inline fun <K, V> Flow<Pair<K, V>>.toMap(destination: MutableMap<K, V> = mutableMapOf()): Map<K, V> {
    collect { (k, v) -> destination[k] = v }
    return destination
}

fun Map<Long, Ballot>.toPreferences(): Flow<Preference> = this.entries.flatMap { (ballotId, ballot) ->
    ballot.ranking.mapIndexed { index, candidate -> Preference(ballotId, candidate, index + 1) }
}.asFlow()

suspend inline fun <A, B> Iterable<A>.mapParallel(crossinline f: suspend (A) -> B): List<B> = coroutineScope {
    map { async { f(it) } }.awaitAll()
}

suspend inline fun <A, B> Iterable<A>.forEachParallel(awaitAll: Boolean = true, crossinline f: suspend (A) -> B): Unit = coroutineScope {
    if (awaitAll) map { async { f(it) } }.awaitAll() else forEach {
        @Suppress("DeferredResultUnused")
        async { f(it) }
    }
}

suspend inline fun <K, V, M : MutableMap<K, V>> Collection<K>.associateWithParallel(crossinline valueSelector: suspend (K) -> V): M {
    val destination = LinkedHashMap<K, V>(this.size)
    this.forEachParallel { element ->
        destination[element] = valueSelector(element)
    }
    @Suppress("UNCHECKED_CAST")
    return destination.toMutableMap() as M
}
