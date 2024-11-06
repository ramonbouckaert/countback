package io.bouckaert.countback

import io.bouckaert.countback.store.BallotStore
import kotlinx.coroutines.flow.Flow

expect fun createFileLoader(): FileLoader

expect fun createBallotStore(preferencesIn: Flow<Preference>): BallotStore
