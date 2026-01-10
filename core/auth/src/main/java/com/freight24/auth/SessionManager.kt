package com.freight24.auth

import kotlinx.coroutines.flow.first
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
         * Synchronous session check.
         * Safe to call from MainActivity during startup.
         */
        fun isSessionValid(): Boolean =
            runBlocking {
                return@runBlocking authDataStore.authToken.first() != null
            }
    }
