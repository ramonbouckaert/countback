package io.bouckaert.countback

import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CountbackTest {

    @BeforeTest
    fun setUp() {
        Candidate.clearCache()
    }

    @Test
    fun brindabella2023Test() = runTest {
        val electionResults = ElectionTest.testRealElectorate(2020, "Brindabella")

        if (electionResults != null) {

            val resigningPile: VotePile = electionResults.winnersAndVotes[Candidate(1, 3, 1, "DAVIS, Johnathan")]!!

            val result = Countback(
                electionResults.ballotStore,
                resigningPile,
                electionResults.quota,
                setOf(
                    Candidate(1, 7, 1, "BAYNHAM, Greg"),
                    Candidate(1, 4, 2, "DANIELS, James"),
                    Candidate(1, 1, 5, "FORDE, Brendan"),
                    Candidate(1, 3, 3, "NUTTALL, Laura"),
                    Candidate(1, 2, 1, "SOXSMITH, Robyn"),
                    Candidate(1, 1, 2, "WERNER-GIBBINGS, Taimus"),
                )
            ).performCount()

            assertEquals(Candidate(1, 3, 3, "NUTTALL, Laura"), result.first)
        }
    }

    @Test
    fun murrumbidgee2022Test() = runTest {
        val electionResults = ElectionTest.testRealElectorate(2020, "Murrumbidgee")

        if (electionResults != null) {

            val resigningPile: VotePile = electionResults.winnersAndVotes[Candidate(4, 7, 2, "JONES, Giulia")]!!

            val result = Countback(
                electionResults.ballotStore,
                resigningPile,
                electionResults.quota,
                setOf(
                    Candidate(4, 7, 4, "COCKS, Ed"),
                    Candidate(4, 7, 1, "SINGH, Amardeep"),
                    Candidate(4, 7, 3, "SUINE, Sarah"),
                )
            ).performCount()

            assertEquals(Candidate(4, 7, 4, "COCKS, Ed"), result.first)
        }
    }

    @Test
    fun yerrabi2021Test() = runTest {
        val electionResults = ElectionTest.testRealElectorate(2020, "Yerrabi")

        if (electionResults != null) {

            val resigningPile: VotePile = electionResults.winnersAndVotes[Candidate(5, 2, 5, "COE, Alistair")]!!

            val result = Countback(
                electionResults.ballotStore,
                resigningPile,
                electionResults.quota,
                setOf(
                    Candidate(5, 2, 2, "MILLIGAN, James"),
                    Candidate(5, 2, 3, "VADAKKEDATHU, Jacob"),
                    Candidate(5, 2, 1, "NADIMPALLI, Krishna")
                )
            ).performCount()

            assertEquals(Candidate(5, 2, 2, "MILLIGAN, James"), result.first)
        }
    }

    @Test
    fun kurrajong2017Test() = runTest {
        val electionResults = ElectionTest.testRealElectorate(2016, "Kurrajong")

        if (electionResults != null) {

            val resigningPile: VotePile = electionResults.winnersAndVotes[Candidate(3, 4, 2, "DOSZPOT, Steve")]!!

            val result = Countback(
                electionResults.ballotStore,
                resigningPile,
                electionResults.quota,
                setOf(
                    Candidate(3, 4, 3, "BURCH, Candice"),
                    Candidate(3, 4, 1, "CURTIN, Brooke"),
                    Candidate(3, 4, 0, "McKAY, Peter")
                )
            ).performCount()

            assertEquals(Candidate(3, 4, 3, "BURCH, Candice"), result.first)
        }
    }

    @Test
    fun yerrabi2019Test() = runTest {
        val electionResults = ElectionTest.testRealElectorate(2016, "Yerrabi")

        if (electionResults != null) {

            val resigningPile: VotePile = electionResults.winnersAndVotes[Candidate(5, 4, 3, "FITZHARRIS, Meegan")]!!

            val result = Countback(
                electionResults.ballotStore,
                resigningPile,
                electionResults.quota,
                setOf(
                    Candidate(5, 4, 2, "GUPTA, Deepak-Raj"),
                    Candidate(5, 5, 0, "WENSING, Veronica"),
                    Candidate(5, 5, 2, "HOLM, Tobias"),
                    Candidate(5, 5, 1, "BRADDOCK, Andrew")
                )
            ).performCount()

            assertEquals(Candidate(5, 4, 2, "GUPTA, Deepak-Raj"), result.first)
        }
    }


    @Test
    fun brindabella2013Test() = runTest {
        val electionResults = ElectionTest.testRealElectorate(2012, "Brindabella")

        if (electionResults != null) {

            val resigningPile: VotePile = electionResults.winnersAndVotes[Candidate(1, 0, 1, "SESELJA, Zed")]!!

            val result = Countback(
                electionResults.ballotStore,
                resigningPile,
                electionResults.quota,
                setOf(
                    Candidate(1, 0, 3, "LAWDER, Nicole"),
                    Candidate(1, 0, 2, "JEFFERY, Val"),
                )
            ).performCount()

            assertEquals(Candidate(1, 0, 3, "LAWDER, Nicole"), result.first)
        }
    }

    @Test
    fun molonglo2014Test() = runTest {
        val electionResults = ElectionTest.testRealElectorate(2012, "Molonglo")

        if (electionResults != null) {

            val resigningPile: VotePile = electionResults.winnersAndVotes[Candidate(3, 5, 6, "GALLAGHER, Katy")]!!

            val result = Countback(
                electionResults.ballotStore,
                resigningPile,
                electionResults.quota,
                setOf(
                    Candidate(3, 5, 1, "FITZHARRIS, Meegan"),
                    Candidate(3, 5, 0, "KULASINGHAM, Mark"),
                    Candidate(3, 5, 3, "DRAKE, Angie"),
                    Candidate(3, 5, 4, "MATHEWS, David"),
                )
            ).performCount()

            assertEquals(Candidate(3, 5, 1, "FITZHARRIS, Meegan"), result.first)
        }
    }

    @Test
    fun ginninderra2016Test() = runTest {
        val electionResults = ElectionTest.testRealElectorate(2012, "Ginninderra")

        if (electionResults != null) {

            val resigningPile: VotePile = electionResults.winnersAndVotes[Candidate(2, 2, 2, "PORTER, Mary")]!!

            val result = Countback(
                electionResults.ballotStore,
                resigningPile,
                electionResults.quota,
                setOf(
                    Candidate(2, 2, 0, "HINDER, Jayson"),
                    Candidate(2, 0, 2, "HUNTER, Meredith"),
                    Candidate(2, 0, 1, "PARRIS, Hannah"),
                    Candidate(2, 0, 0, "HIGGINS, James"),
                )
            ).performCount()

            assertEquals(Candidate(2, 2, 0, "HINDER, Jayson"), result.first)
        }
    }

    @Test
    fun brindabella2016Test() = runTest {
        val electionResults = ElectionTest.testRealElectorate(2012, "Brindabella")

        if (electionResults != null) {

            val resigningPile: VotePile = electionResults.winnersAndVotes[Candidate(1, 0, 4, "SMYTH, Brendan")]!!

            val result = Countback(
                electionResults.ballotStore,
                resigningPile,
                electionResults.quota,
                setOf(
                    Candidate(1, 0, 2, "JEFFERY, Val"),
                    Candidate(1, 2, 1, "CODY, Rebecca"),
                    Candidate(1, 2, 2, "MAFTOUM, Karl"),
                    Candidate(1, 2, 4, "KINNIBURGH, Mike"),
                )
            ).performCount()

            assertEquals(Candidate(1, 0, 2, "JEFFERY, Val"), result.first)
        }
    }

    @Test
    fun ginninderra2011Test() = runTest {
        val electionResults = ElectionTest.testRealElectorate(2008, "Ginninderra")

        if (electionResults != null) {

            val resigningPile: VotePile = electionResults.winnersAndVotes[Candidate(2, 1, 2, "STANHOPE, Jon")]!!

            val result = Countback(
                electionResults.ballotStore,
                resigningPile,
                electionResults.quota,
                setOf(
                    Candidate(2, 1, 3, "BOURKE, Chris"),
                    Candidate(2, 1, 0, "CIRSON, Adina"),
                )
            ).performCount()

            assertEquals(Candidate(2, 1, 3, "BOURKE, Chris"), result.first)
        }
    }
}