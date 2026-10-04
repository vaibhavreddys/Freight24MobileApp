package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class IndentInterestInboxResponse(
    val totalIndents: Int,
     val data: List<IndentInterestInboxData>,
): BaseResponse() {

    @Serializable
    data class IndentInterestInboxData(
        val indentId: String,
        val indentGroupId: String,
        val truckerUserId: String,
        val interestCount: Int,
        val load: LoadDetails,
        val truckers: List<Trucker>
    )

    @Serializable
    data class LoadDetails(
       // `load_weight` is DECIMAL(10,2) and the cost columns are numeric on
       // the backend, so these arrive as JSON numbers. They were typed as
       // String here, which only parsed because the shared Json instance runs
       // isLenient = true — and it cost us Indian digit grouping, since
       // "₹$clientCost" renders 40000 rather than ₹40,000.
       val material: String,
       val weight: Double? = null,
       val indentCode: String,
       val truck: TruckDetails,
       val pricing: Pricing,
       val client: Client,
       val priority: String,
       val expiresAt: String,
       val loading: LoadingDetails,
       val unloading: LoadingDetails,
    )

    @Serializable
    data class Pricing(
        val clientCost: Double? = null,
        val businessProposedCost: Double? = null,
        val negotiatedCost: Double? = null
    )

    @Serializable
    data class TruckDetails(
        val type: String,
        val displayName: String
    )

    @Serializable
    data class LoadingDetails(
        val city: String?,
        val area: String?
    )

    @Serializable
    data class Trucker(
        val interestId: String,
        val name: String,
        val phone: String,
        val preferredTruckId: String?,
        val expressedAt: String,
        val remarks: String?
    )

    @Serializable
    data class Client(
        val name: String,
        val tagCode: String,
    )
}