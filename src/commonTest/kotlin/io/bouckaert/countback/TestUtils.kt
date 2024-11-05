package io.bouckaert.countback

import io.bouckaert.countback.store.BallotStore

expect fun createFileLoader(): FileLoader

expect fun createBallotStore(ballotsIn: Sequence<Ballot>): BallotStore
