package com.freight24.rest.response

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoadBoardResponse(
    val success: Boolean,
    val data: List<CityLoadSummary>?,
) {
    @Serializable
    data class CityLoadSummary(
        @SerialName("operational_city")
        @SerializedName("operational_city")
        val operationalCity: String?,
        @SerialName("operational_city_id")
        @SerializedName("operational_city_id")
        val operationalCityId: Int?,
        @SerialName("total_loads")
        @SerializedName("total_loads")
        val totalLoads: Int?,
        @SerialName("latest_loads")
        @SerializedName("latest_loads")
        val latestLoads: List<LatestLoad>?,
    )

    @Serializable
    data class LatestLoad(
        @SerialName("indent_id")
        @SerializedName("indent_id")
        val indentId: String?,
        val price: Double?,
        val weight: Double?,
        val material: String?,
        val priority: String?,
        @SerialName("tat_hours")
        @SerializedName("tat_hours")
        val tatHours: Int?,
        @SerialName("created_at")
        @SerializedName("created_at")
        val createdAt: String,
        @SerialName("truck_type")
        @SerializedName("truck_type")
        val truckType: String,
        @SerialName("tat_enabled")
        @SerializedName("tat_enabled")
        val tatEnabled: Boolean,
        @SerialName("loading_area")
        @SerializedName("loading_area")
        val loadingArea: String?,
        @SerialName("loading_city")
        @SerializedName("loading_city")
        val loadingCity: String?,
        @SerialName("unloading_area")
        @SerializedName("unloading_area")
        val unloadingArea: String?,
        @SerialName("unloading_city")
        @SerializedName("unloading_city")
        val unloadingCity: String?,
        @SerialName("employee_name")
        @SerializedName("employee_name")
        val employeeName: String?,
        @SerialName("employee_phone")
        @SerializedName("employee_phone")
        val employeePhone: String?,
    )
}
