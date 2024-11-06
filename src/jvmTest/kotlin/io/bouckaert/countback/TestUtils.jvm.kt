package io.bouckaert.countback

import io.bouckaert.countback.store.BallotStore
import io.bouckaert.countback.store.InMemoryBallotStore
import kotlinx.coroutines.flow.Flow

actual fun createFileLoader(): FileLoader = JvmFileLoader()
actual fun createBallotStore(preferencesIn: Flow<Preference>): BallotStore = InMemoryBallotStore(preferencesIn)
