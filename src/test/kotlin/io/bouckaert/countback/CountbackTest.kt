package io.bouckaert.countback

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class CountbackTest {

    @Test
    fun murrumbidgee2022Test() = runTest {
        val electionResults = ElectionTest.testRealElectorate(2020, "Murrumbidgee")

        val resigningPile: VotePile = electionResults.winnersAndVotes[Candidate("JONES, Giulia")]!!

        val result = Countback(
            resigningPile,
            electionResults.quota,
            setOf(
                Candidate("COCKS, Ed"),
                Candidate("SINGH, Amardeep"),
                Candidate("SUINE, Sarah"),
            )
        ).performCount()

        assertEquals(Candidate("COCKS, Ed"), result.first)
    }

    @Test
    fun yerrabi2021Test() = runTest {
        val electionResults = ElectionTest.testRealElectorate(2020, "Yerrabi")

        val resigningPile: VotePile = electionResults.winnersAndVotes[Candidate("COE, Alistair")]!!

        val result = Countback(
            resigningPile,
            electionResults.quota,
            setOf(
                Candidate("MILLIGAN, James"),
                Candidate("VADAKKEDATHU, Jacob"),
                Candidate("NADIMPALLI, Krishna")
            )
        ).performCount()

        assertEquals(Candidate("MILLIGAN, James"), result.first)
    }

    @Test
    fun kurrajong2017Test() = runTest {
        val electionResults = ElectionTest.testRealElectorate(2016, "Kurrajong")

        val resigningPile: VotePile = electionResults.winnersAndVotes[Candidate("DOSZPOT, Steve")]!!

        val result = Countback(
            resigningPile,
            electionResults.quota,
            setOf(
                Candidate("BURCH, Candice"),
                Candidate("CURTIN, Brooke"),
                Candidate("McKAY, Peter")
            )
        ).performCount()

        assertEquals(Candidate("BURCH, Candice"), result.first)
    }

    @Test
    fun yerrabi2019Test() = runTest {
        val electionResults = ElectionTest.testRealElectorate(2016, "Yerrabi")

        val resigningPile: VotePile = electionResults.winnersAndVotes[Candidate("FITZHARRIS, Meegan")]!!

        val result = Countback(
            resigningPile,
            electionResults.quota,
            setOf(
                Candidate("GUPTA, Deepak-Raj"),
                Candidate("WENSING, Veronica"),
                Candidate("HOLM, Tobias"),
                Candidate("BRADDOCK, Andrew")
            )
        ).performCount()

        assertEquals(Candidate("GUPTA, Deepak-Raj"), result.first)
    }



    @Test
    fun brindabella2013Test() = runTest {
        val electionResults = ElectionTest.testRealElectorate(2012, "Brindabella")

        val resigningPile: VotePile = electionResults.winnersAndVotes[Candidate("SESELJA, Zed")]!!

        val result = Countback(
            resigningPile,
            electionResults.quota,
            setOf(
                Candidate("LAWDER, Nicole"),
                Candidate("JEFFERY, Val"),
            )
        ).performCount()

        assertEquals(Candidate("LAWDER, Nicole"), result.first)
    }

    @Test
    fun molonglo2014Test() = runTest {
        val electionResults = ElectionTest.testRealElectorate(2012, "Molonglo")

        val resigningPile: VotePile = electionResults.winnersAndVotes[Candidate("GALLAGHER, Katy")]!!

        val result = Countback(
            resigningPile,
            electionResults.quota,
            setOf(
                Candidate("FITZHARRIS, Meegan"),
                Candidate("KULASINGHAM, Mark"),
                Candidate("DRAKE, Angie"),
                Candidate("MATHEWS, David"),
            )
        ).performCount()

        assertEquals(Candidate("FITZHARRIS, Meegan"), result.first)
    }

    @Test
    fun ginninderra2016Test() = runTest {
        val electionResults = ElectionTest.testRealElectorate(2012, "Ginninderra")

        val resigningPile: VotePile = electionResults.winnersAndVotes[Candidate("PORTER, Mary")]!!

        val result = Countback(
            resigningPile,
            electionResults.quota,
            setOf(
                Candidate("HINDER, Jayson"),
                Candidate("HUNTER, Meredith"),
                Candidate("PARRIS, Hannah"),
                Candidate("HIGGINS, James"),
            )
        ).performCount()

        assertEquals(Candidate("HINDER, Jayson"), result.first)
    }

    @Test
    fun brindabella2016Test() = runTest {
        val electionResults = ElectionTest.testRealElectorate(2012, "Brindabella")

        val resigningPile: VotePile = electionResults.winnersAndVotes[Candidate("SMYTH, Brendan")]!!

        val result = Countback(
            resigningPile,
            electionResults.quota,
            setOf(
                Candidate("JEFFERY, Val"),
                Candidate("CODY, Rebecca"),
                Candidate("MAFTOUM, Karl"),
                Candidate("KINNIBURGH, Mike"),
            )
        ).performCount()

        assertEquals(Candidate("JEFFERY, Val"), result.first)
    }

    @Test
    fun ginninderra2011Test() = runTest {
        val electionResults = ElectionTest.testRealElectorate(2008, "Ginninderra")

        val resigningPile: VotePile = electionResults.winnersAndVotes[Candidate("STANHOPE, Jon")]!!

        val result = Countback(
            resigningPile,
            electionResults.quota,
            setOf(
                Candidate("BOURKE, Chris"),
                Candidate("CIRSON, Adina"),
            )
        ).performCount()

        assertEquals(Candidate("BOURKE, Chris"), result.first)
    }
}