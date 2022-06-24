package io.bouckaert.countback

import org.junit.Test
import java.io.File
import java.io.OutputStream
import kotlin.test.assertEquals

class ElectionTest {

    companion object {
        fun testRealElectorate(year: Int, electorate: String, verbose: Boolean = false): Election.Results {
            val classLoader = Thread.currentThread().contextClassLoader

            val dataLoader = ACTDataLoader {
                classLoader.getResourceAsStream("electiondata/$year.zip")!!
            }

            val candidatesMap = dataLoader.loadCandidates(
                dataLoader.loadElectorates()
            )[electorate]!!

            val votes = dataLoader.loadBallots(electorate, candidatesMap)

            val election = Election(
                numberOfVacancies = if (electorate == "Molonglo") 7 else 5,
                candidates = candidatesMap.values.toSet(),
                ballots = votes,
                roundCountToInt = year <= 2012
            )

            val outputStream = if (verbose) System.out else OutputStream.nullOutputStream()

            return election.performCount(
                verbose = verbose,
                outputStream = outputStream
            )
        }
    }

    @Test
    fun simpleTest() {
        val cPlatypus = Candidate("Platypus")
        val cWombat = Candidate("Wombat")
        val cEmu = Candidate("Emu")
        val cKoala = Candidate("Koala")

        val election = Election(
            numberOfVacancies = 2,
            candidates = setOf(cPlatypus, cWombat, cEmu, cKoala),
            ballots = listOf(
                Ballot(linkedSetOf(cPlatypus, cKoala, cWombat, cEmu)),
                Ballot(linkedSetOf(cPlatypus, cKoala, cWombat, cEmu)),
                Ballot(linkedSetOf(cWombat, cEmu, cKoala, cPlatypus)),
                Ballot(linkedSetOf(cKoala, cPlatypus, cEmu, cWombat)),
                Ballot(linkedSetOf(cEmu, cWombat, cPlatypus, cKoala)),
                Ballot(linkedSetOf(cEmu, cPlatypus, cWombat, cKoala)),
                Ballot(linkedSetOf(cPlatypus, cKoala, cEmu, cWombat)),
                Ballot(linkedSetOf(cEmu, cWombat, cPlatypus, cKoala)),
            )
        )

        val result = election.performCount()

        assertEquals(result.winnersAndVotes.keys, setOf(cPlatypus, cEmu))
    }

    // 2020 Election

    @Test
    fun murrumbidgee2020Test() {
        assertEquals(
            setOf(
                Candidate("HANSON, Jeremy"),
                Candidate("STEEL, Chris"),
                Candidate("JONES, Giulia"),
                Candidate("PATERSON, Marisa"),
                Candidate("DAVIDSON, Emma"),
            ),
            testRealElectorate(2020, "Murrumbidgee").winnersAndVotes.keys
        )
    }

    @Test
    fun kurrajong2020Test() {
        assertEquals(
            setOf(
                Candidate("BARR, Andrew"),
                Candidate("RATTENBURY, Shane"),
                Candidate("STEPHEN-SMITH, Rachel"),
                Candidate("LEE, Elizabeth"),
                Candidate("VASSAROTTI, Rebecca"),
            ),
            testRealElectorate(2020,"Kurrajong").winnersAndVotes.keys
        )
    }

    @Test
    fun brindabella2020Test() {
        assertEquals(
            setOf(
                Candidate("GENTLEMAN, Mick"),
                Candidate("BURCH, Joy"),
                Candidate("PARTON, Mark"),
                Candidate("LAWDER, Nicole"),
                Candidate("DAVIS, Johnathan"),
            ),
            testRealElectorate(2020, "Brindabella").winnersAndVotes.keys
        )
    }

    @Test
    fun yerrabi2020Test() {
        assertEquals(
            setOf(
                Candidate("COE, Alistair"),
                Candidate("ORR, Suzanne"),
                Candidate("PETTERSSON, Michael"),
                Candidate("BRADDOCK, Andrew"),
                Candidate("CASTLEY, Leanne"),
            ),
            testRealElectorate(2020, "Yerrabi").winnersAndVotes.keys
        )
    }

    @Test
    fun ginninderra2020Test() {
        assertEquals(
            setOf(
                Candidate("BERRY, Yvette"),
                Candidate("KIKKERT, Elizabeth"),
                Candidate("CHEYNE, Tara"),
                Candidate("CLAY, Jo"),
                Candidate("CAIN, Peter"),
            ),
            testRealElectorate(2020, "Ginninderra").winnersAndVotes.keys
        )
    }

    // 2016 Election

    @Test
    fun murrumbidgee2016Test() {
        assertEquals(
            setOf(
                Candidate("HANSON, Jeremy"),
                Candidate("STEEL, Chris"),
                Candidate("JONES, Giulia"),
                Candidate("CODY, Bec"),
                Candidate("LE COUTEUR, Caroline"),
            ),
            testRealElectorate(2016, "Murrumbidgee").winnersAndVotes.keys
        )
    }

    @Test
    fun kurrajong2016Test() {
        assertEquals(
            setOf(
                Candidate("BARR, Andrew"),
                Candidate("RATTENBURY, Shane"),
                Candidate("STEPHEN-SMITH, Rachel"),
                Candidate("LEE, Elizabeth"),
                Candidate("DOSZPOT, Steve"),
            ),
            testRealElectorate(2016,"Kurrajong").winnersAndVotes.keys,
        )
    }

    @Test
    fun brindabella2016Test() {
        assertEquals(
            setOf(
                Candidate("GENTLEMAN, Mick"),
                Candidate("BURCH, Joy"),
                Candidate("PARTON, Mark"),
                Candidate("LAWDER, Nicole"),
                Candidate("WALL, Andrew"),
            ),
            testRealElectorate(2016, "Brindabella").winnersAndVotes.keys
        )
    }

    @Test
    fun yerrabi2016Test() {
        assertEquals(
            setOf(
                Candidate("COE, Alistair"),
                Candidate("ORR, Suzanne"),
                Candidate("PETTERSSON, Michael"),
                Candidate("FITZHARRIS, Meegan"),
                Candidate("MILLIGAN, James"),
            ),
            testRealElectorate(2016, "Yerrabi").winnersAndVotes.keys
        )
    }

    @Test
    fun ginninderra2016Test() {
        assertEquals(
            setOf(
                Candidate("BERRY, Yvette"),
                Candidate("DUNNE, Vicki"),
                Candidate("CHEYNE, Tara"),
                Candidate("KIKKERT, Elizabeth"),
                Candidate("RAMSAY, Gordon"),
            ),
            testRealElectorate(2016, "Ginninderra").winnersAndVotes.keys
        )
    }

    // 2012 Election

    @Test
    fun molonglo2012Test() {
        assertEquals(
            setOf(
                Candidate("BARR, Andrew"),
                Candidate("CORBELL, Simon"),
                Candidate("GALLAGHER, Katy"),
                Candidate("HANSON, Jeremy"),
                Candidate("DOSZPOT, Steve"),
                Candidate("JONES, Giulia"),
                Candidate("RATTENBURY, Shane"),
            ),
            testRealElectorate(2012,"Molonglo").winnersAndVotes.keys,
        )
    }

    @Test
    fun brindabella2012Test() {
        assertEquals(
            setOf(
                Candidate("GENTLEMAN, Mick"),
                Candidate("BURCH, Joy"),
                Candidate("SESELJA, Zed"),
                Candidate("SMYTH, Brendan"),
                Candidate("WALL, Andrew"),
            ),
            testRealElectorate(2012, "Brindabella").winnersAndVotes.keys
        )
    }


    @Test
    fun ginninderra2012Test() {
        assertEquals(
            setOf(
                Candidate("BERRY, Yvette"),
                Candidate("DUNNE, Vicki"),
                Candidate("COE, Alistair"),
                Candidate("BOURKE, Chris"),
                Candidate("PORTER, Mary"),
            ),
            testRealElectorate(2012, "Ginninderra").winnersAndVotes.keys
        )
    }

    // 2008 Election

    @Test
    fun molonglo2008Test() {
        assertEquals(
            setOf(
                Candidate("BARR, Andrew"),
                Candidate("CORBELL, Simon"),
                Candidate("GALLAGHER, Katy"),
                Candidate("HANSON, Jeremy"),
                Candidate("Le COUTEUR, Caroline"),
                Candidate("SESELJA, Zed"),
                Candidate("RATTENBURY, Shane"),
            ),
            testRealElectorate(2008,"Molonglo").winnersAndVotes.keys,
        )
    }

    @Test
    fun brindabella2008Test() {
        assertEquals(
            setOf(
                Candidate("BRESNAN, Amanda"),
                Candidate("BURCH, Joy"),
                Candidate("DOSZPOT, Steve"),
                Candidate("HARGREAVES, John"),
                Candidate("SMYTH, Brendan"),
            ),
            testRealElectorate(2008, "Brindabella").winnersAndVotes.keys
        )
    }


    @Test
    fun ginninderra2008Test() {
        assertEquals(
            setOf(
                Candidate("STANHOPE, Jon"),
                Candidate("DUNNE, Vicki"),
                Candidate("COE, Alistair"),
                Candidate("HUNTER, Meredith"),
                Candidate("PORTER, Mary"),
            ),
            testRealElectorate(2008, "Ginninderra").winnersAndVotes.keys
        )
    }
}