package com.freight24.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

private const val KEYSTORE_ALIAS = "freight24_auth_master_key"
private const val ANDROID_KEYSTORE = "AndroidKeyStore"
private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val IV_SEPARATOR = ":"

val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs_secure")

@Singleton
class AuthDataStore
    @Inject
        constructor(
        @param:ApplicationContext private val context: Context,
    ) {
        companion object {
            private val TOKEN_KEY = stringPreferencesKey("encrypted_token")
            private val REFRESH_TOKEN_KEY = stringPreferencesKey("encrypted_refresh_token")
            private val PHONE_KEY = stringPreferencesKey("encrypted_phone")
            private val USER_ID_KEY = stringPreferencesKey("encrypted_user_id")
            private val INSTALLATION_ID_KEY = stringPreferencesKey("encrypted_installation_id")
            private val ACCESS_TOKEN_EXPIRES_AT_KEY = longPreferencesKey("access_token_expires_at")
            private val REFRESH_TOKEN_EXPIRES_AT_KEY = longPreferencesKey("refresh_token_expires_at")
            private val REFRESH_SESSION_ID_KEY = stringPreferencesKey("encrypted_refresh_session_id")
            private val REFRESH_FAMILY_ID_KEY = stringPreferencesKey("encrypted_refresh_family_id")
        }

        private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

        private fun getOrCreateSecretKey(): SecretKey {
            if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
                return (keyStore.getEntry(KEYSTORE_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
            }

            val keyGenerator = KeyGenerator.getInstance("AES", ANDROID_KEYSTORE)
            val parameterSpec =
                KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setRandomizedEncryptionRequired(true) // Security best practice
                    .build()

            keyGenerator.init(parameterSpec)
            return keyGenerator.generateKey()
        }

        private fun encrypt(data: String): String {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
            val iv = cipher.iv
            val encrypted = cipher.doFinal(data.toByteArray(Charsets.UTF_8))

            val ivBase64 = Base64.encodeToString(iv, Base64.DEFAULT).trim()
            val encryptedBase64 = Base64.encodeToString(encrypted, Base64.DEFAULT).trim()

            return "$ivBase64$IV_SEPARATOR$encryptedBase64"
        }

        private fun decrypt(encryptedWithIv: String): String? {
            return try {
                val (ivBase64, encryptedBase64) = encryptedWithIv.split(IV_SEPARATOR, limit = 2)
                val iv = Base64.decode(ivBase64, Base64.DEFAULT)
                val encrypted = Base64.decode(encryptedBase64, Base64.DEFAULT)

                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(128, iv))
                val decrypted = cipher.doFinal(encrypted)
                String(decrypted, Charsets.UTF_8)
            } catch (e: Exception) {
                null // Invalid/corrupted data → treat as not logged in
            }
        }

        // Public flows (decrypted)
        val authToken: Flow<String?> =
            context.authDataStore.data.map { prefs ->
                prefs[TOKEN_KEY]?.let { decrypt(it) }
            }

        val refreshToken: Flow<String?> =
            context.authDataStore.data.map { prefs ->
                prefs[REFRESH_TOKEN_KEY]?.let { decrypt(it) }
            }

        val phoneNumber: Flow<String?> =
            context.authDataStore.data.map { prefs ->
                prefs[PHONE_KEY]?.let { decrypt(it) }
            }

        val userId: Flow<String?> =
            context.authDataStore.data.map { prefs ->
                prefs[USER_ID_KEY]?.let { decrypt(it) }
            }

        val installationId: Flow<String?> =
            context.authDataStore.data.map { prefs ->
                prefs[INSTALLATION_ID_KEY]?.let { decrypt(it) }
            }

        val accessTokenExpiresAt: Flow<Long?> =
            context.authDataStore.data.map { prefs ->
                prefs[ACCESS_TOKEN_EXPIRES_AT_KEY]
            }

        val refreshTokenExpiresAt: Flow<Long?> =
            context.authDataStore.data.map { prefs ->
                prefs[REFRESH_TOKEN_EXPIRES_AT_KEY]
            }

        val refreshSessionId: Flow<String?> =
            context.authDataStore.data.map { prefs ->
                prefs[REFRESH_SESSION_ID_KEY]?.let { decrypt(it) }
            }

        val refreshFamilyId: Flow<String?> =
            context.authDataStore.data.map { prefs ->
                prefs[REFRESH_FAMILY_ID_KEY]?.let { decrypt(it) }
            }

        suspend fun ensureInstallationId(): String {
            val existingInstallationId =
                context.authDataStore.data.first()[INSTALLATION_ID_KEY]?.let { decrypt(it) }

            if (!existingInstallationId.isNullOrBlank()) {
                return existingInstallationId
            }

            val generatedInstallationId = UUID.randomUUID().toString()
            context.authDataStore.edit { prefs ->
                prefs[INSTALLATION_ID_KEY] = encrypt(generatedInstallationId)
            }
            return generatedInstallationId
        }

        suspend fun saveAuthData(
            token: String,
            phone: String,
            userId: String,
            installationId: String? = null,
            refreshToken: String? = null,
            accessTokenExpiresAtMillis: Long? = null,
            refreshTokenExpiresAtMillis: Long? = null,
            refreshSessionId: String? = null,
            refreshFamilyId: String? = null,
        ) {
            context.authDataStore.edit { prefs ->
                prefs[TOKEN_KEY] = encrypt(token)
                prefs[PHONE_KEY] = encrypt(phone)
                prefs[USER_ID_KEY] = encrypt(userId)

                if (!refreshToken.isNullOrBlank()) {
                    prefs[REFRESH_TOKEN_KEY] = encrypt(refreshToken)
                } else {
                    prefs.remove(REFRESH_TOKEN_KEY)
                }

                if (!installationId.isNullOrBlank()) {
                    prefs[INSTALLATION_ID_KEY] = encrypt(installationId)
                }

                if (accessTokenExpiresAtMillis != null) {
                    prefs[ACCESS_TOKEN_EXPIRES_AT_KEY] = accessTokenExpiresAtMillis
                } else {
                    prefs.remove(ACCESS_TOKEN_EXPIRES_AT_KEY)
                }

                if (refreshTokenExpiresAtMillis != null) {
                    prefs[REFRESH_TOKEN_EXPIRES_AT_KEY] = refreshTokenExpiresAtMillis
                } else {
                    prefs.remove(REFRESH_TOKEN_EXPIRES_AT_KEY)
                }

                if (!refreshSessionId.isNullOrBlank()) {
                    prefs[REFRESH_SESSION_ID_KEY] = encrypt(refreshSessionId)
                } else {
                    prefs.remove(REFRESH_SESSION_ID_KEY)
                }

                if (!refreshFamilyId.isNullOrBlank()) {
                    prefs[REFRESH_FAMILY_ID_KEY] = encrypt(refreshFamilyId)
                } else {
                    prefs.remove(REFRESH_FAMILY_ID_KEY)
                }
            }
        }

        suspend fun clearAuthData() {
            context.authDataStore.edit { prefs ->
                prefs.remove(TOKEN_KEY)
                prefs.remove(REFRESH_TOKEN_KEY)
                prefs.remove(PHONE_KEY)
                prefs.remove(USER_ID_KEY)
                prefs.remove(ACCESS_TOKEN_EXPIRES_AT_KEY)
                prefs.remove(REFRESH_TOKEN_EXPIRES_AT_KEY)
                prefs.remove(REFRESH_SESSION_ID_KEY)
                prefs.remove(REFRESH_FAMILY_ID_KEY)
            }
        }
    }
