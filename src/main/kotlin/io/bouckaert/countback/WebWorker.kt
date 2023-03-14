package io.bouckaert.countback

import org.w3c.dom.DedicatedWorkerGlobalScope
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.MessageEvent

external val self: DedicatedWorkerGlobalScope

fun main() {
    self.addEventListener("message", { event ->
        if (event is MessageEvent) {
            GlobalScope.launch {
                try {
                    handleMessage(Json.decodeFromString(event.data as String)) { response ->
                        self.postMessage(Json.encodeToString(response))
                    }
                } catch (t: Throwable) {
                    self.postMessage(
                        Json.encodeToString(
                            WebWorkerResponse.Error(null, t) as WebWorkerResponse
                        )
                    )
                }
            }
        }
    })
    self.postMessage(Json.encodeToString(WebWorkerResponse.Ready() as WebWorkerResponse))
}

private suspend fun handleMessage(request: WebWorkerRequest, respond: (response: WebWorkerResponse) -> Unit) {
    when (request) {
        is WebWorkerRequest.Candidates -> respond(
            WebWorkerResponse.Candidates(
                request.id,
                loadCandidates(request.year)
            )
        )
        is WebWorkerRequest.Countback -> {
            when {
                request.candidatesToContest.isEmpty() -> respond(
                    WebWorkerResponse.Error(
                        request.id,
                        "You must specify at least one candidate to contest the vacancy"
                    )
                )
                else -> {
                    respond(
                        WebWorkerResponse.Countback(
                            request.id,
                            "Loading ballot data into memory, please be patient..."
                        )
                    )
                    val election = loadElectionData(request.year, request.electorate).let {
                        Election(
                            if (request.electorate == "Molonglo") 7 else 5,
                            it.first,
                            it.second,
                            request.year <= 2012
                        )
                    }
                    respond(
                        WebWorkerResponse.Countback(
                            request.id,
                            "*** Simulating Initial Distribution of Preferences ***",
                            true
                        )
                    )
                    val initialDistribution =
                        election.performCount(verbose = true, writeOutput = { message, newParagraph ->
                            respond(
                                WebWorkerResponse.Countback(
                                    request.id,
                                    message,
                                    newParagraph
                                )
                            )
                        })
                    val winningCandidates = initialDistribution.winnersAndVotes.keys
                    respond(WebWorkerResponse.Countback(request.id, "Winning Candidates:", true))
                    winningCandidates.forEach { respond(WebWorkerResponse.Countback(request.id, "$it")) }
                    val resigningCandidatePile =
                        initialDistribution.winnersAndVotes[request.candidateToResign]
                    val filteredCandidatesToContest =
                        request.candidatesToContest.filter { c -> c !in winningCandidates }.toSet()
                    when {
                        filteredCandidatesToContest.isEmpty() ->
                            respond(
                                WebWorkerResponse.Countback(
                                    request.id,
                                    "There are no candidates eligible to contest the countback.",
                                    true
                                )
                            )
                        resigningCandidatePile == null ->
                            respond(
                                WebWorkerResponse.Countback(
                                    request.id,
                                    "${request.candidateToResign} was not in the field of winning candidates.",
                                    true
                                )
                            )
                        else -> {
                            respond(WebWorkerResponse.Countback(request.id, "*** Starting countback ***", true))
                            Countback(
                                resigningCandidatePile,
                                initialDistribution.quota,
                                filteredCandidatesToContest
                            ).performCount(verbose = true, writeOutput = { message, newParagraph ->
                                respond(
                                    WebWorkerResponse.Countback(
                                        request.id,
                                        message,
                                        newParagraph
                                    )
                                )
                            })
                        }
                    }

                    respond(WebWorkerResponse.End(request.id))
                }
            }
        }
    }
}

private suspend fun loadCandidates(year: Int): Map<String, Collection<Candidate>> {
    val dataLoader = ACTDataLoader("electiondata/$year/")

    return dataLoader.loadCandidates(
        dataLoader.loadElectorates()
    ).mapValues { it.value.values }
}

private suspend fun loadElectionData(year: Int, electorate: String): Pair<Set<Candidate>, List<Ballot>> {
    val dataLoader = ACTDataLoader("electiondata/$year/")

    val candidatesMap = dataLoader.loadCandidates(
        dataLoader.loadElectorates()
    )[electorate]!!

    val ballots = dataLoader.loadBallots(electorate, candidatesMap)

    return candidatesMap.values.toSet() to ballots
}