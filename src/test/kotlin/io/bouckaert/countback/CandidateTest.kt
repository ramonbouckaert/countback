package io.bouckaert.countback

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CandidateTest {
    @BeforeTest
    fun setUp() {
        Candidate.clearCache()
    }

    @Test
    fun crudAndCache() {
        val ecode = (1..10).random()
        val pcode = (1..10).random()
        val ccode = (1..20).random()
        val name = List(20) { ('A'..'Z').random() }.joinToString("")

        val candidate = Candidate(ecode, pcode, ccode, name)

        assertEquals(ecode, candidate.electorateCode)
        assertEquals(pcode, candidate.partyCode)
        assertEquals(ccode, candidate.candidateCode)
        assertEquals(name, candidate.toString())

        val sameCandidateFromSameData = Candidate(ecode, pcode, ccode, name)
        assertEquals(candidate, sameCandidateFromSameData)
        assertEquals(ecode, sameCandidateFromSameData.electorateCode)
        assertEquals(pcode, sameCandidateFromSameData.partyCode)
        assertEquals(ccode, sameCandidateFromSameData.candidateCode)
        assertEquals(name, sameCandidateFromSameData.toString())

        val sameCandidateFromCache = Candidate(ecode, pcode, ccode)
        assertEquals(candidate, sameCandidateFromCache)
        assertEquals(ecode, sameCandidateFromCache.electorateCode)
        assertEquals(pcode, sameCandidateFromCache.partyCode)
        assertEquals(ccode, sameCandidateFromCache.candidateCode)
        assertEquals(name, sameCandidateFromCache.toString())

        val sameCandidateFromId = Candidate(candidate.id)
        assertEquals(candidate, sameCandidateFromId)
        assertEquals(ecode, sameCandidateFromId.electorateCode)
        assertEquals(pcode, sameCandidateFromId.partyCode)
        assertEquals(ccode, sameCandidateFromId.candidateCode)
        assertEquals(name, sameCandidateFromId.toString())
    }
}