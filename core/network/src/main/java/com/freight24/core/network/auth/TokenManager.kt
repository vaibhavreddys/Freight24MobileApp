package com.freight24.core.network.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class AuthEvent {
    LOGOUT
}

@Singleton
class TokenManager @Inject constructor(
    private val authDataStore: AuthDataStore
) {
    val authToken: Flow<String?> = authDataStore.authToken

    private val _authEvents = MutableSharedFlow<AuthEvent>()
    val authEvents: SharedFlow<AuthEvent> = _authEvents.asSharedFlow()

    suspend fun clearAuth() {
        authDataStore.clearAuthData()
        _authEvents.emit(AuthEvent.LOGOUT)
    }
}
