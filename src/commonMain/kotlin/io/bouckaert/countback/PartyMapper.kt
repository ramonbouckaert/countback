package io.bouckaert.countback

import kotlin.jvm.JvmStatic

interface PartyMapper {
    companion object {
        const val LIBERAL = "\uD83D\uDD35"
        const val LABOR = "\uD83D\uDD34"
        const val GREEN = "\uD83D\uDFE2"
        const val IND = "⚪"
        @JvmStatic
        fun forYear(year: Int): PartyMapper? = when(year) {
            2020 -> object : PartyMapper {
                override fun findEmoji(electorateCode: Int, partyCode: Int): String = when(electorateCode) {
                    1 -> when(partyCode) {
                        1 -> LABOR
                        3 -> GREEN
                        4 -> LIBERAL
                        else -> IND
                    }
                    2 -> when(partyCode) {
                        7 -> LIBERAL
                        9 -> GREEN
                        10 -> LABOR
                        else -> IND
                    }
                    3 -> when(partyCode) {
                        1 -> GREEN
                        3 -> LIBERAL
                        5 -> LABOR
                        else -> IND
                    }
                    4 -> when(partyCode) {
                        3 -> GREEN
                        5 -> LABOR
                        7 -> LIBERAL
                        else -> IND
                    }
                    5 -> when(partyCode) {
                        2 -> LIBERAL
                        3 -> GREEN
                        8 -> LABOR
                        else -> IND
                    }
                    else -> IND
                }
            }
            2016 -> object : PartyMapper {
                override fun findEmoji(electorateCode: Int, partyCode: Int): String = when(electorateCode) {
                    1 -> when(partyCode) {
                        1 -> LIBERAL
                        4 -> LABOR
                        6 -> GREEN
                        else -> IND
                    }
                    2 -> when(partyCode) {
                        1 -> LABOR
                        2 -> GREEN
                        3 -> LIBERAL
                        else -> IND
                    }
                    3 -> when(partyCode) {
                        0 -> LABOR
                        2 -> GREEN
                        4 -> LIBERAL
                        else -> IND
                    }
                    4 -> when(partyCode) {
                        1 -> LABOR
                        3 -> LIBERAL
                        7 -> GREEN
                        else -> IND
                    }
                    5 -> when(partyCode) {
                        1 -> LIBERAL
                        4 -> LABOR
                        5 -> GREEN
                        else -> IND
                    }
                    else -> IND
                }
            }
            2012 -> object : PartyMapper {
                override fun findEmoji(electorateCode: Int, partyCode: Int): String = when(electorateCode) {
                    1 -> when(partyCode) {
                        0 -> LIBERAL
                        1 -> GREEN
                        2 -> LABOR
                        else -> IND
                    }
                    2 -> when(partyCode) {
                        0 -> GREEN
                        2 -> LABOR
                        5 -> LIBERAL
                        else -> IND
                    }
                    3 -> when(partyCode) {
                        0 -> GREEN
                        5 -> LABOR
                        3 -> LIBERAL
                        else -> IND
                    }
                    else -> IND
                }
            }
            2008 -> object : PartyMapper {
                override fun findEmoji(electorateCode: Int, partyCode: Int): String = when(electorateCode) {
                    1 -> when(partyCode) {
                        0 -> LIBERAL
                        3 -> GREEN
                        4 -> LABOR
                        else -> IND
                    }
                    2 -> when(partyCode) {
                        1 -> LABOR
                        3 -> GREEN
                        4 -> LIBERAL
                        else -> IND
                    }
                    3 -> when(partyCode) {
                        1 -> LABOR
                        6 -> GREEN
                        7 -> LIBERAL
                        else -> IND
                    }
                    else -> IND
                }
            }
            else -> null
        }
    }
    fun findEmoji(electorateCode: Int, partyCode: Int): String?
}