package io.bouckaert.countback

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class WebWorkerRequest {
    abstract val id: String

    @Serializable
    @SerialName("candidates")
    data class Candidates(
        override val id: String,
        val year: Int
    ) : WebWorkerRequest()

    @Serializable
    @SerialName("countback")
    data class Countback(
        override val id: String,
        val year: Int,
        val electorate: String,
        val candidateToResign: Candidate,
        val candidatesToContest: List<Candidate>
    ) : WebWorkerRequest()
}
