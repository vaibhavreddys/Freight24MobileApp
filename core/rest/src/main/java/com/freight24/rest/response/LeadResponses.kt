package com.freight24.rest.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LeadDto(
    val id: String = "",
    val batchId: String? = null,
    val phone: String = "",
    val phoneRaw: String? = null,
    val name: String? = null,
    val status: String = "new",
    val lastCallOutcome: String? = null,
    val callAttempts: Int = 0,
    val baseCity: LeadCityDto? = null,
    val assignedTo: String? = null,
    val assignedAt: String? = null,
    val assignedUser: LeadUserDto? = null,
    val truckTypes: List<LeadTruckTypeDto> = emptyList(),
    val notInterestedReason: String? = null,
    val invalidReason: String? = null,
    val truckOwnerId: String? = null,
    val isArchived: Boolean = false,
    val notes: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class LeadCityDto(
    val id: Int = 0,
    val name: String = "",
)

@Serializable
data class LeadTruckTypeDto(
    val id: Int = 0,
    val truckType: String = "",
    val displayName: String = "",
)

@Serializable
data class LeadUserDto(
    val id: String = "",
    val tagCode: String? = null,
    val fullName: String? = null,
)

@Serializable
data class LeadPaginationDto(
    val page: Int = 1,
    val limit: Int = 20,
    val total: Int = 0,
    @SerialName("total_pages") val totalPages: Int = 1,
)

@Serializable
data class LeadRouteDto(
    val id: String = "",
    val sourceCity: LeadCityDto? = null,
    val destinationCity: LeadCityDto? = null,
    val priority: Int = 1,
)

@Serializable
data class LeadActivityDto(
    val id: String = "",
    val leadId: String = "",
    val actor: LeadUserDto? = null,
    val actionType: String = "",
    val statusFrom: String? = null,
    val statusTo: String? = null,
    val notes: String? = null,
    val metadata: Map<String, kotlinx.serialization.json.JsonElement>? = null,
    val createdAt: String? = null,
)

@Serializable
data class LeadBatchDto(
    val id: String = "",
    @SerialName("file_name") val fileName: String? = null,
    val total_rows: Int = 0,
    val valid_rows: Int = 0,
    val invalid_rows: Int = 0,
    val status: String? = null,
    val created_at: String? = null,
)

@Serializable
data class LeadDetailDto(
    val id: String = "",
    val batchId: String? = null,
    val phone: String = "",
    val phoneRaw: String? = null,
    val name: String? = null,
    val status: String = "new",
    val lastCallOutcome: String? = null,
    val callAttempts: Int = 0,
    val baseCity: LeadCityDto? = null,
    val assignedTo: String? = null,
    val assignedUser: LeadUserDto? = null,
    val truckTypes: List<LeadTruckTypeDto> = emptyList(),
    val routes: List<LeadRouteDto> = emptyList(),
    val activities: List<LeadActivityDto> = emptyList(),
    val notInterestedReason: String? = null,
    val invalidReason: String? = null,
    val truckOwnerId: String? = null,
    val isArchived: Boolean = false,
    val notes: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val batch: LeadBatchDto? = null,
)

@Serializable
class LeadListResponse : BaseResponse() {
    @SerialName("data")
    val data: List<LeadDto> = emptyList()

    @SerialName("pagination")
    val pagination: LeadPaginationDto = LeadPaginationDto()

    @SerialName("counts")
    val counts: Map<String, Int> = emptyMap()
}

@Serializable
class LeadDetailResponse : BaseResponse() {
    @SerialName("data")
    val data: LeadDetailDto = LeadDetailDto()
}

@Serializable
data class LeadSummaryData(
    @SerialName("status_counts") val statusCounts: Map<String, Int> = emptyMap(),
    @SerialName("new_count") val newCount: Int = 0,
    @SerialName("follow_up_count") val followUpCount: Int = 0,
    @SerialName("badge_count") val badgeCount: Int = 0,
)

@Serializable
class LeadSummaryResponse : BaseResponse() {
    @SerialName("data")
    val data: LeadSummaryData = LeadSummaryData()
}

@Serializable
data class CallOutcomeData(
    @SerialName("lead_id") val leadId: String = "",
    @SerialName("call_attempts") val callAttempts: Int = 0,
    val status: String = "",
    @SerialName("max_attempts_reached") val maxAttemptsReached: Boolean = false,
    @SerialName("max_call_attempts") val maxCallAttempts: Int = 3,
)

@Serializable
class CallOutcomeResponse : BaseResponse() {
    @SerialName("data")
    val data: CallOutcomeData = CallOutcomeData()
}

@Serializable
data class ConvertLeadData(
    @SerialName("lead_id") val leadId: String = "",
    @SerialName("truck_owner_user_id") val truckOwnerUserId: String = "",
    @SerialName("truck_owner_profile_id") val truckOwnerProfileId: String = "",
    @SerialName("tag_code") val tagCode: String = "",
)

@Serializable
class ConvertLeadResponse : BaseResponse() {
    @SerialName("data")
    val data: ConvertLeadData = ConvertLeadData()
}
