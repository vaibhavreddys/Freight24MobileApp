package com.freight24.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager
    @Inject
    constructor(
        private val authDataStore: AuthDataStore,
    ) {
        /**
         * Flow of logged in state.
         */
        val isLoggedIn: Flow<Boolean> = authDataStore.authToken.map { it != null }

        /**
         * Synchronous session check.
         * Safe to call from MainActivity during startup.
         */
        fun isSessionValid(): Boolean =
            runBlocking {
                return@runBlocking authDataStore.authToken.first() != null
            }
    }
