package com.freight24.auth

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager
    @Inject
    constructor(
        private val authDataStore: AuthDataStore,
    ) {
        val authToken: Flow<String?> = authDataStore.authToken

        suspend fun clearAuth() {
            authDataStore.clearAuthData()
        }
    }
