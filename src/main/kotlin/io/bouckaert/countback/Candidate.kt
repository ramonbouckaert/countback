package io.bouckaert.countback

import kotlinx.serialization.Serializable

@Serializable
value class Candidate(val name: String) {
    override fun toString(): String = name
}