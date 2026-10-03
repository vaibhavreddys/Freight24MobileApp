package com.freight24.rest.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoadBoardResponse(
    val success: Boolean,
    val data: LoadBoardData?,
) {

    @Serializable
    data class LoadBoardData (
        val groups: List<CityLoadSummary>?,
    )

    @Serializable
    data class CityLoadSummary(
        @SerialName("groupName")
        val operationalCity: String?,
        @SerialName("operationalCityId")
        val operationalCityId: Int?,
        @SerialName("totalLoads")
        val totalLoads: Int?,
        @SerialName("latestLoads")
        val latestLoads: List<LatestLoad>?,
    )

    @Serializable
    data class LatestLoad(
        val indentId: String?,
        val price: Double?,
        val weight: Double?,
        val material: String?,
        val priority: String?,
        val tat: Tat?,
        val createdAt: String,
        val truckType: TruckType,
        val loading: Loading,
        val unloading: UnLoading,
        val employee: Employee?,
        //@SerialName("is_interested")
        val isInterested: Boolean? = null,
        //@SerialName("interest_status")
        val interestStatus: String? = null,
    )

    @Serializable
    data class Tat(
        val hours: Int?,
        val enabled: Boolean?,
    )

    @Serializable
    data class TruckType(
        val id: Int,
        val name: String,
        val bodyType: String,
        val displayName: String,
    )


    @Serializable
    data class Loading(
        val area: String?,
        val city: String?,
        val address: String?,
        val contactPhone: String?,
        val contactPerson: String?,
    )

    @Serializable
    data class Employee(
        val name: String?,
        val phone: String?
    )

    @Serializable
    data class UnLoading(
        val area: String?,
        val city: String?,
    )


}
