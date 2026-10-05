package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class TruckerInboxInterestResponse(
    val data: List<TruckerInboxInterestInfo>,
    val total: Int,
) : BaseResponse() {
    @Serializable
    data class TruckerInboxInterestInfo(
        val interestId: String,
        val name: String,
        val phone: String?,
        val expressedAt: String,
    )
}
