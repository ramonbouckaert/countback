package io.bouckaert.countback

import io.bouckaert.countback.store.InMemoryBallotStore
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ElectionTest {
    companion object {
        suspend fun testRealElectorate(year: Int, electorate: String): Election.Results? {
            try {
                val dataLoader = ACTDataLoader(
                    "electiondata/$year/",
                    createFileLoader()
                )

                val electoratesMap = dataLoader.loadElectorates()

                val ecode = electoratesMap.entries.find { it.value == electorate }!!.key

                val candidates = dataLoader.loadCandidates()

                val ballotStore = createBallotStore(dataLoader.loadPreferences(electorate, ecode))

                val election = Election(
                    numberOfVacancies = if (electorate == "Molonglo") 7 else 5,
                    candidates = candidates,
                    ballotStore,
                    roundCountToInt = year <= 2012
                )

                return election.performCount()
            } catch (e: FileLoader.FileLoadException) {
                return null
            }
        }
    }

    @BeforeTest
    fun setUp() {
        Candidate.clearCache()
    }

    @Test
    fun simpleTest() = runTest {
        val cPlatypus = Candidate(1, 1, 1, "Platypus")
        val cWombat = Candidate(1, 1, 2, "Wombat")
        val cEmu = Candidate(1, 1, 3, "Emu")
        val cKoala = Candidate(1, 1, 4, "Koala")

        val election = Election(
            numberOfVacancies = 2,
            candidates = setOf(cPlatypus, cWombat, cEmu, cKoala),
            ballotStore = InMemoryBallotStore(
                mapOf(
                    1L to Ballot(arrayOf(cPlatypus, cKoala, cWombat, cEmu)),
                    2L to Ballot(arrayOf(cPlatypus, cKoala, cWombat, cEmu)),
                    3L to Ballot(arrayOf(cWombat, cEmu, cKoala, cPlatypus)),
                    4L to Ballot(arrayOf(cKoala, cPlatypus, cEmu, cWombat)),
                    5L to Ballot(arrayOf(cEmu, cWombat, cPlatypus, cKoala)),
                    6L to Ballot(arrayOf(cEmu, cPlatypus, cWombat, cKoala)),
                    7L to Ballot(arrayOf(cPlatypus, cKoala, cEmu, cWombat)),
                    8L to Ballot(arrayOf(cEmu, cWombat, cPlatypus, cKoala)),
                ).toPreferences()
            )
        )

        val result = election.performCount()

        assertEquals(setOf(cPlatypus, cEmu), result.winnersAndVotes.keys)
    }

    // 2020 Election

    @Test
    fun murrumbidgee2020Test() = runTest {
        val results = testRealElectorate(2020, "Murrumbidgee")
        if (results != null) {
            assertEquals(
                setOf(
                    Candidate(4, 7, 5, "HANSON, Jeremy"),
                    Candidate(4, 5, 3, "STEEL, Chris"),
                    Candidate(4, 7, 2, "JONES, Giulia"),
                    Candidate(4, 5, 4, "PATERSON, Marisa"),
                    Candidate(4, 3, 3, "DAVIDSON, Emma"),
                ),
                results.winnersAndVotes.keys
            )
        }
    }

    @Test
    fun kurrajong2020Test() = runTest {
        val results = testRealElectorate(2020, "Kurrajong")
        if (results != null) {
            assertEquals(
                setOf(
                    Candidate(3, 5, 3, "BARR, Andrew"),
                    Candidate(3, 1, 3, "RATTENBURY, Shane"),
                    Candidate(3, 5, 1, "STEPHEN-SMITH, Rachel"),
                    Candidate(3, 3, 3, "LEE, Elizabeth"),
                    Candidate(3, 1, 4, "VASSAROTTI, Rebecca"),
                ),
                results.winnersAndVotes.keys
            )
        }
    }

    @Test
    fun brindabella2020Test() = runTest {
        val results = testRealElectorate(2020, "Brindabella")
        if (results != null) {
            assertEquals(
                setOf(
                    Candidate(1, 1, 4, "GENTLEMAN, Mick"),
                    Candidate(1, 1, 3, "BURCH, Joy"),
                    Candidate(1, 4, 1, "PARTON, Mark"),
                    Candidate(1, 4, 5, "LAWDER, Nicole"),
                    Candidate(1, 3, 1, "DAVIS, Johnathan"),
                ),
                results.winnersAndVotes.keys
            )
        }
    }

    @Test
    fun yerrabi2020Test() = runTest {
        val results = testRealElectorate(2020, "Yerrabi")
        if (results != null) {
            assertEquals(
                setOf(
                    Candidate(5, 2, 5, "COE, Alistair"),
                    Candidate(5, 8, 3, "ORR, Suzanne"),
                    Candidate(5, 8, 5, "PETTERSSON, Michael"),
                    Candidate(5, 3, 1, "BRADDOCK, Andrew"),
                    Candidate(5, 2, 4, "CASTLEY, Leanne"),
                ),
                results.winnersAndVotes.keys
            )
        }
    }

    @Test
    fun ginninderra2020Test() = runTest {
        val results = testRealElectorate(2020, "Ginninderra")
        if (results != null) {
            assertEquals(
                setOf(
                    Candidate(2, 10, 2, "BERRY, Yvette"),
                    Candidate(2, 7, 2, "KIKKERT, Elizabeth"),
                    Candidate(2, 10, 1, "CHEYNE, Tara"),
                    Candidate(2, 9, 2, "CLAY, Jo"),
                    Candidate(2, 7, 5, "CAIN, Peter"),
                ),
                results.winnersAndVotes.keys
            )
        }
    }

    // 2016 Election

    @Test
    fun murrumbidgee2016Test() = runTest {
        val results = testRealElectorate(2016, "Murrumbidgee")
        if (results != null) {
            assertEquals(
                setOf(
                    Candidate(4, 3, 3, "HANSON, Jeremy"),
                    Candidate(4, 1, 1, "STEEL, Chris"),
                    Candidate(4, 3, 4, "JONES, Giulia"),
                    Candidate(4, 1, 4, "CODY, Bec"),
                    Candidate(4, 7, 1, "LE COUTEUR, Caroline"),
                ),
                results.winnersAndVotes.keys
            )
        }
    }

    @Test
    fun kurrajong2016Test() = runTest {
        val results = testRealElectorate(2016, "Kurrajong")
        if (results != null) {
            assertEquals(
                setOf(
                    Candidate(3, 0, 2, "BARR, Andrew"),
                    Candidate(3, 2, 0, "RATTENBURY, Shane"),
                    Candidate(3, 0, 1, "STEPHEN-SMITH, Rachel"),
                    Candidate(3, 4, 4, "LEE, Elizabeth"),
                    Candidate(3, 4, 2, "DOSZPOT, Steve"),
                ),
                results.winnersAndVotes.keys,
            )
        }
    }

    @Test
    fun brindabella2016Test() = runTest {
        val results = testRealElectorate(2016, "Brindabella")
        if (results != null) {
            assertEquals(
                setOf(
                    Candidate(1, 4, 1, "GENTLEMAN, Mick"),
                    Candidate(1, 4, 0, "BURCH, Joy"),
                    Candidate(1, 1, 1, "PARTON, Mark"),
                    Candidate(1, 1, 4, "LAWDER, Nicole"),
                    Candidate(1, 1, 3, "WALL, Andrew"),
                ),
                results.winnersAndVotes.keys
            )
        }
    }

    @Test
    fun yerrabi2016Test() = runTest {
        val results = testRealElectorate(2016, "Yerrabi")
        if (results != null) {
            assertEquals(
                setOf(
                    Candidate(5, 1, 0, "COE, Alistair"),
                    Candidate(5, 4, 1, "ORR, Suzanne"),
                    Candidate(5, 4, 0, "PETTERSSON, Michael"),
                    Candidate(5, 4, 3, "FITZHARRIS, Meegan"),
                    Candidate(5, 1, 3, "MILLIGAN, James"),
                ),
                results.winnersAndVotes.keys
            )
        }
    }

    @Test
    fun ginninderra2016Test() = runTest {
        val results = testRealElectorate(2016, "Ginninderra")
        if (results != null) {
            assertEquals(
                setOf(
                    Candidate(2, 1, 0, "BERRY, Yvette"),
                    Candidate(2, 3, 4, "DUNNE, Vicki"),
                    Candidate(2, 1, 1, "CHEYNE, Tara"),
                    Candidate(2, 3, 2, "KIKKERT, Elizabeth"),
                    Candidate(2, 1, 2, "RAMSAY, Gordon"),
                ),
                results.winnersAndVotes.keys
            )
        }
    }

    // 2012 Election

    @Test
    fun molonglo2012Test() = runTest {
        val results = testRealElectorate(2012, "Molonglo")
        if (results != null) {
            assertEquals(
                setOf(
                    Candidate(3, 5, 5, "BARR, Andrew"),
                    Candidate(3, 5, 2, "CORBELL, Simon"),
                    Candidate(3, 5, 6, "GALLAGHER, Katy"),
                    Candidate(3, 3, 5, "HANSON, Jeremy"),
                    Candidate(3, 3, 1, "DOSZPOT, Steve"),
                    Candidate(3, 3, 6, "JONES, Giulia"),
                    Candidate(3, 0, 2, "RATTENBURY, Shane"),
                ),
                results.winnersAndVotes.keys,
            )
        }
    }

    @Test
    fun brindabella2012Test() = runTest {
        val results = testRealElectorate(2012, "Brindabella")
        if (results != null) {
            assertEquals(
                setOf(
                    Candidate(1, 2, 3, "GENTLEMAN, Mick"),
                    Candidate(1, 2, 0, "BURCH, Joy"),
                    Candidate(1, 0, 1, "SESELJA, Zed"),
                    Candidate(1, 0, 4, "SMYTH, Brendan"),
                    Candidate(1, 0, 0, "WALL, Andrew"),
                ),
                results.winnersAndVotes.keys
            )
        }
    }


    @Test
    fun ginninderra2012Test() = runTest {
        val results = testRealElectorate(2012, "Ginninderra")
        if (results != null) {
            assertEquals(
                setOf(
                    Candidate(2, 2, 3, "BERRY, Yvette"),
                    Candidate(2, 5, 4, "DUNNE, Vicki"),
                    Candidate(2, 5, 0, "COE, Alistair"),
                    Candidate(2, 2, 1, "BOURKE, Chris"),
                    Candidate(2, 2, 2, "PORTER, Mary"),
                ),
                results.winnersAndVotes.keys
            )
        }
    }

    // 2008 Election

    @Test
    fun molonglo2008Test() = runTest {
        val results = testRealElectorate(2008, "Molonglo")
        if (results != null) {
            assertEquals(
                setOf(
                    Candidate(3, 1, 2, "BARR, Andrew"),
                    Candidate(3, 1, 1, "CORBELL, Simon"),
                    Candidate(3, 1, 4, "GALLAGHER, Katy"),
                    Candidate(3, 7, 2, "HANSON, Jeremy"),
                    Candidate(3, 6, 1, "Le COUTEUR, Caroline"),
                    Candidate(3, 7, 1, "SESELJA, Zed"),
                    Candidate(3, 6, 0, "RATTENBURY, Shane"),
                ),
                results.winnersAndVotes.keys,
            )
        }
    }

    @Test
    fun brindabella2008Test() = runTest {
        val results = testRealElectorate(2008, "Brindabella")
        if (results != null) {
            assertEquals(
                setOf(
                    Candidate(1, 3, 0, "BRESNAN, Amanda"),
                    Candidate(1, 4, 1, "BURCH, Joy"),
                    Candidate(1, 0, 3, "DOSZPOT, Steve"),
                    Candidate(1, 4, 4, "HARGREAVES, John"),
                    Candidate(1, 0, 0, "SMYTH, Brendan"),
                ),
                results.winnersAndVotes.keys
            )
        }
    }


    @Test
    fun ginninderra2008Test() = runTest {
        val results = testRealElectorate(2008, "Ginninderra")
        if (results != null) {
            assertEquals(
                setOf(
                    Candidate(2, 1, 2, "STANHOPE, Jon"),
                    Candidate(2, 4, 0, "DUNNE, Vicki"),
                    Candidate(2, 4, 2, "COE, Alistair"),
                    Candidate(2, 3, 1, "HUNTER, Meredith"),
                    Candidate(2, 1, 1, "PORTER, Mary"),
                ),
                results.winnersAndVotes.keys
            )
        }
    }
}