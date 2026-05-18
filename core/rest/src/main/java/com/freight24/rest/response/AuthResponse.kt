package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: AuthData? = null,
)

@Serializable
data class AuthData(
    val token: String? = null,
    val access_token: String? = null,
    val refresh_token: String? = null,
    val user: User? = null,
    val token_type: String? = null,
    val expires_in: Int? = null,
    val refresh_expires_in: Int? = null,
    val refresh_expires_at: String? = null,
    val refresh_session_id: String? = null,
    val refresh_family_id: String? = null,
    val otp_code: String? = null,
)

@Serializable
data class User(
    val id: String,
    val tag_code: String,
    val phone: String? = null,
    val active_role: String? = null,
    val roles: List<String> = emptyList(),
    val profile: Profile? = null,
    val created_at: String? = null,
)

@Serializable
data class Profile(
    val id: String? = null,
    val user_id: String,
    val full_name: String?,
    val email: String? = null,
    val phone: String? = null,
    val alt_phones: List<String>? = null,
    val pan_card: String? = null,
    val aadhar_card: String? = null,
    val business_name: String? = null,
    val gst_number: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val created_by: String? = null,
    val status: String? = null,
    val is_tds_applicable: Boolean = false,
)
