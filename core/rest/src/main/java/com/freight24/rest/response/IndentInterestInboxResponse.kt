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
       val material: String,
       val weight: String,
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
        val clientCost: String?,
        val businessProposedCost: String?,
        val negotiatedCost: String?
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