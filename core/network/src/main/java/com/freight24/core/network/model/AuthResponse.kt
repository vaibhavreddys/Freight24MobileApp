package com.freight24.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: AuthData? = null
)

@Serializable
data class AuthData(
    val token: String? = null,
    val user: User? = null,
    val token_type: String? = null,
    val expires_in: Int? = null,
    val otp_code: String? = null
)

@Serializable
data class User(
    val id: String,
    val tag_code: String,
    val phone: String,
    val active_role: String? = null,           // ← NEW: active_role as String
    val roles: List<String> = emptyList(),     // ← CHANGED: List<String> instead of List<Role>
    val profile: Profile? = null,
    val created_at: String? = null
)

@Serializable
data class Profile(
    val id: String,
    val user_id: String,
    val full_name: String?,
    val email: String? = null,
    val phone: String,
    val alt_phones: List<String>? = null,      // ← Was null, now List or null
    val pan_card: String? = null,
    val aadhar_card: String? = null,
    val business_name: String? = null,
    val gst_number: String? = null,
    val created_at: String,
    val updated_at: String,
    val created_by: String,
    val updated_by: String,
    val status: String,
    val is_tds_applicable: Boolean
)