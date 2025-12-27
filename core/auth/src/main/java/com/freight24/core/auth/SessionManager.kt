package com.freight24.core.auth

import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val authRepository: AuthRepository
) {

    /**
     * Synchronous session check.
     * Safe to call from MainActivity during startup.
     */
    fun isSessionValid(): Boolean = runBlocking {
        val token = authRepository.getSavedToken()
        !token.isNullOrBlank()
    }
}
