package io.bouckaert.countback

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class WebWorkerResponse {
    abstract val id: String?

    @Serializable
    @SerialName("ready")
    data class Ready(
        override val id: String? = null
    ): WebWorkerResponse()

    @Serializable
    @SerialName("error")
    data class Error(
        override val id: String?,
        val message: String,
        val stackTrace: String? = null
    ): WebWorkerResponse() {
        constructor(id: String?, throwable: Throwable): this(
            id,
            throwable.message ?: throwable::class.simpleName ?: "Unknown",
            throwable.stackTraceToString()
        )
    }

    @Serializable
    @SerialName("candidates")
    data class Candidates(
        override val id: String,
        val candidatesByElectorate: Map<String, Collection<CandidateWithName>>
    ): WebWorkerResponse()

    @Suppress("DataClassPrivateConstructor")
    @Serializable
    @SerialName("candidateWithName")
    data class CandidateWithName private constructor(
        val id: Candidate,
        val name: String
    ) {
        constructor(candidate: Candidate): this(candidate, candidate.toString())
    }

    @Serializable
    @SerialName("countback")
    data class Countback(
        override val id: String,
        val message: String,
        val newParagraph: Boolean = false
    ): WebWorkerResponse()

    @Serializable
    @SerialName("end")
    data class End(
        override val id: String
    ): WebWorkerResponse()
}
