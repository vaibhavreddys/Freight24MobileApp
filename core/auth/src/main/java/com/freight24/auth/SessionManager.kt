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

        /**
         * Persists a fresh session after OTP verification succeeds.
         * The new login A/B flow writes through SessionManager so the app keeps
         * one public session API even though the underlying storage is encrypted DataStore.
         */
        suspend fun saveSession(
            token: String,
            phone: String,
            userId: String,
        ) {
            authDataStore.saveAuthData(token, phone, userId)
        }

        suspend fun logout() {
            authDataStore.clearAuthData()
        }
    }
