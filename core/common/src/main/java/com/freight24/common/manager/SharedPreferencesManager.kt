package com.freight24.common.manager

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesManager
    @Inject
    constructor(
        @param:ApplicationContext val context: Context,
    ) : DevicePreferences {
        private val prefs: SharedPreferences =
            context.getSharedPreferences("freight24_app_prefs", Context.MODE_PRIVATE)

        private fun saveString(
            key: String,
            value: String,
        ) {
            prefs.edit { putString(key, value) }
        }

        private fun getString(
            key: String,
            defaultValue: String? = null,
        ): String? {
            return prefs.getString(key, defaultValue)
        }

        private fun saveInt(
            key: String,
            value: Int,
        ) {
            prefs.edit { putInt(key, value) }
        }

        private fun getInt(
            key: String,
            defaultValue: Int = 0,
        ): Int {
            return prefs.getInt(key, defaultValue)
        }

        private fun saveBoolean(
            key: String,
            value: Boolean,
        ) {
            prefs.edit { putBoolean(key, value) }
        }

        private fun getBoolean(
            key: String,
            defaultValue: Boolean = false,
        ): Boolean {
            return prefs.getBoolean(key, defaultValue)
        }

        override var firebaseToken: String
            get() = getString(KEY_FIREBASE_TONE) ?: ""
            set(value) {
                saveString(KEY_FIREBASE_TONE, value)
            }

        companion object {
            // --- Example Keys ---
            const val KEY_FIREBASE_TONE = "firebase_token"
        }
    }
