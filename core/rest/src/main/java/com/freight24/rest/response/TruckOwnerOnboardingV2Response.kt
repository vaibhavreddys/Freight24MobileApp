package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class TruckOwnerOnboardingV2Response(
    val success: Boolean,
    val message: String,
    val resumed: Boolean = false,
    val nextStep: String? = null,
    val data: TruckOwnerOnboardingV2Data? = null,
)

@Serializable
data class TruckOwnerOnboardingV2Data(
    val id: String,
    val profileId: String? = null,
    val tagCode: String? = null,
    val fullName: String? = null,
    val status: String? = null,
)
