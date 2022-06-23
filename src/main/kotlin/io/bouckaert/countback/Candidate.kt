package io.bouckaert.countback

@JvmInline
value class Candidate(val name: String) {
    override fun toString(): String = name
}