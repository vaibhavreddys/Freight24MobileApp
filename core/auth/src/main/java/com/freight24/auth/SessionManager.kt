package com.freight24.auth

import com.freight24.rest.api.AuthSessionApi
import com.freight24.rest.request.LogoutRequest
import com.freight24.rest.request.RefreshSessionRequest
import com.freight24.rest.response.AuthData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager
    @Inject
    constructor(
        private val authDataStore: AuthDataStore,
        private val authSessionApi: AuthSessionApi,
    ) {
        private val sessionScope = CoroutineScope(Job() + Dispatchers.IO)
        private val refreshMutex = Mutex()
        private var proactiveRefreshJob: Job? = null

        private val _lastRefreshError = MutableStateFlow<String?>(null)
        val lastRefreshError = _lastRefreshError.asStateFlow()

        fun consumeLastRefreshError(): String? {
            val message = _lastRefreshError.value
            _lastRefreshError.value = null
            return message
        }

        /**
         * Flow of logged in state.
         */
        val isLoggedIn: Flow<Boolean> =
            combine(authDataStore.authToken, authDataStore.refreshToken) { token, refreshToken ->
                !token.isNullOrBlank() || !refreshToken.isNullOrBlank()
            }.distinctUntilChanged()

        /**
         * Safe to call from MainActivity during startup.
         */
        fun isSessionValid(): Boolean =
            runBlocking {
                val token = authDataStore.authToken.first()
                val refreshToken = authDataStore.refreshToken.first()
                !token.isNullOrBlank() || !refreshToken.isNullOrBlank()
            }

        /**
         * Get the stored user ID synchronously.
         */
        fun getUserId(): String? =
            runBlocking {
                return@runBlocking authDataStore.userId.first()
            }

        /**
         * The role this session was opened with, or null when the backend did not
         * return one (e.g. a session seeded before roles were persisted).
         */
        fun getActiveRole(): String? =
            runBlocking {
                return@runBlocking authDataStore.activeRole.first()
            }

        /** Every role granted to this session. */
        fun getRoles(): List<String> =
            runBlocking {
                return@runBlocking authDataStore.roles.first()
            }

        /**
         * True when the session's role is a fleet executive.
         *
         * The backend carries this role under two names: `roles.name` is
         * `fleet_executive` (what the auth response returns) while
         * `employee_profiles.designation` is `fleet_exec`. Accept either so a
         * session seeded from either source gates identically.
         */
        fun isFleetExecutive(): Boolean {
            val candidates = buildList {
                getActiveRole()?.let { add(it) }
                addAll(getRoles())
            }
            return candidates.any { role ->
                val normalized = role.trim().lowercase()
                normalized == FLEET_EXECUTIVE_ROLE || normalized == FLEET_EXEC_ROLE
            }
        }

        suspend fun ensureInstallationId(): String = authDataStore.ensureInstallationId()

        suspend fun saveSession(
            token: String,
            phone: String,
            userId: String,
        ) {
            val authData = AuthData(token = token)
            saveSession(
                phone = phone,
                userId = userId,
                authData = authData,
                installationId = authDataStore.ensureInstallationId(),
            )
        }

        suspend fun saveSession(
            phone: String,
            userId: String,
            authData: AuthData,
            installationId: String? = null,
        ) {
            val accessToken = authData.access_token ?: authData.token.orEmpty()
            require(accessToken.isNotBlank()) { "Access token missing from auth response" }

            val now = System.currentTimeMillis()
            val accessTokenExpiresAtMillis = authData.expires_in?.let { now + TimeUnit.SECONDS.toMillis(it.toLong()) }
            val refreshTokenExpiresAtMillis = authData.refresh_expires_in?.let { now + TimeUnit.SECONDS.toMillis(it.toLong()) }
            val resolvedInstallationId = installationId ?: authDataStore.ensureInstallationId()

            authDataStore.saveAuthData(
                token = accessToken,
                phone = phone,
                userId = userId,
                installationId = resolvedInstallationId,
                refreshToken = authData.refresh_token,
                accessTokenExpiresAtMillis = accessTokenExpiresAtMillis,
                refreshTokenExpiresAtMillis = refreshTokenExpiresAtMillis,
                refreshSessionId = authData.refresh_session_id,
                refreshFamilyId = authData.refresh_family_id,
                activeRole = authData.user?.active_role,
                roles = authData.user?.roles.orEmpty(),
            )

            _lastRefreshError.value = null
            scheduleProactiveRefresh()
        }

        suspend fun bootstrapSession(): Boolean {
            authDataStore.ensureInstallationId()
            return refreshIfNeeded(forceRefresh = false)
        }

        suspend fun refreshSession(): Boolean {
            return refreshIfNeeded(forceRefresh = true)
        }

        suspend fun logout() {
            val refreshToken = authDataStore.refreshToken.first().orEmpty()
            val installationId = authDataStore.ensureInstallationId()

            if (refreshToken.isNotBlank()) {
                try {
                    authSessionApi.logout(
                        LogoutRequest(
                            refreshToken = refreshToken,
                            installationId = installationId,
                        ),
                    )
                } catch (_: Exception) {
                    // Logout should still clear local state even if the backend call fails.
                }
            }

            clearLocalSession()
        }

        private suspend fun refreshIfNeeded(forceRefresh: Boolean): Boolean =
            refreshMutex.withLock {
                val now = System.currentTimeMillis()
                val accessToken = authDataStore.authToken.first().orEmpty()
                val refreshToken = authDataStore.refreshToken.first().orEmpty()
                val accessTokenExpiresAt = authDataStore.accessTokenExpiresAt.first()
                val hadPersistedSession =
                    accessToken.isNotBlank() ||
                        refreshToken.isNotBlank() ||
                        authDataStore.phoneNumber.first().orEmpty().isNotBlank() ||
                        authDataStore.userId.first().orEmpty().isNotBlank() ||
                        accessTokenExpiresAt != null ||
                        authDataStore.refreshTokenExpiresAt.first() != null

                val accessTokenValid =
                    accessToken.isNotBlank() &&
                        (accessTokenExpiresAt == null || accessTokenExpiresAt > now)

                if (refreshToken.isBlank()) {
                    if (!accessTokenValid) {
                        if (hadPersistedSession) {
                            _lastRefreshError.value = "Session expired, please sign in again"
                        }
                        clearLocalSession()
                        return false
                    }
                    return true
                }

                val refreshBufferMs = TimeUnit.MINUTES.toMillis(5)
                val shouldRefresh =
                    forceRefresh ||
                        accessTokenExpiresAt == null ||
                        (accessTokenExpiresAt - now) <= refreshBufferMs

                if (!shouldRefresh) {
                    scheduleProactiveRefresh()
                    return true
                }

                val installationId = authDataStore.ensureInstallationId()
                return try {
                    val response =
                        authSessionApi.refreshSession(
                            RefreshSessionRequest(
                                refreshToken = refreshToken,
                                installationId = installationId,
                            ),
                        )
                    val data = response.data
                    if (response.success && data != null) {
                        val phone = authDataStore.phoneNumber.first().orEmpty()
                        val userId = authDataStore.userId.first().orEmpty()

                        if (phone.isNotBlank() && userId.isNotBlank()) {
                            saveSession(
                                phone = phone,
                                userId = userId,
                                authData = data,
                                installationId = installationId,
                            )
                            true
                        } else {
                            if (!accessTokenValid) {
                                _lastRefreshError.value = "Session expired, please sign in again"
                                clearLocalSession()
                                false
                            } else {
                                scheduleProactiveRefresh()
                                true
                            }
                        }
                    } else {
                        if (!accessTokenValid) {
                            _lastRefreshError.value = "Session expired, please sign in again"
                            clearLocalSession()
                            false
                        } else {
                            true
                        }
                    }
                } catch (throwable: Exception) {
                    if (!accessTokenValid) {
                        _lastRefreshError.value = "Session expired, please sign in again"
                        clearLocalSession()
                        false
                    } else {
                        true
                    }
                }
            }

        private fun scheduleProactiveRefresh() {
            proactiveRefreshJob?.cancel()
            proactiveRefreshJob =
                sessionScope.launch {
                    val refreshToken = authDataStore.refreshToken.first().orEmpty()
                    val accessTokenExpiresAt = authDataStore.accessTokenExpiresAt.first()

                    if (refreshToken.isBlank() || accessTokenExpiresAt == null) {
                        return@launch
                    }

                    val refreshBufferMs = TimeUnit.MINUTES.toMillis(5)
                    val delayMs = (accessTokenExpiresAt - System.currentTimeMillis() - refreshBufferMs).coerceAtLeast(0L)
                    if (delayMs > 0L) {
                        delay(delayMs)
                    }

                    refreshSession()
                }
        }

        private suspend fun clearLocalSession() {
            proactiveRefreshJob?.cancel()
            proactiveRefreshJob = null
            // Drop any pending refresh error so it cannot leak to the next launch.
            // Once the local session is gone the user is effectively signed out;
            // the message would only appear on a future login screen as stale noise.
            _lastRefreshError.value = null
            authDataStore.clearAuthData()
        }

        companion object {
            /** `roles.name` as returned by the backend auth response. */
            const val FLEET_EXECUTIVE_ROLE = "fleet_executive"

            /** `employee_profiles.designation` for the same role. */
            const val FLEET_EXEC_ROLE = "fleet_exec"
        }
    }
