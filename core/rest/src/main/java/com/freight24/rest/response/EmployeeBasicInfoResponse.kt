package com.freight24.rest.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmployeeBasicInfoResponse(
    val success: Boolean,
    val message: String,
    val data: EmployeeBasicInfo,
)

@Serializable
data class EmployeeBasicInfo(
    @SerialName("user_id") val userId: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("designation") val designation: String? = null,
    @SerialName("status") val status: String,
    @SerialName("tag_code") val tagCode: String = "",
    @SerialName("department") val department: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("email") val email: String? = null,
)
