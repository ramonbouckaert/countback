package io.bouckaert.countback

import io.bouckaert.countback.store.BallotStore
import io.bouckaert.countback.store.InMemoryBallotStore

actual fun createFileLoader(): FileLoader = JsFileLoader()
actual fun createBallotStore(ballotsIn: Sequence<Ballot>): BallotStore = InMemoryBallotStore(ballotsIn)
