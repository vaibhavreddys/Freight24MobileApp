package com.freight24.rest.response

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable


data class LoadBoardResponse(
    val success: Boolean,
    val data: List<CityLoadSummary>?,
) {

    @Serializable
    data class CityLoadSummary(
        @SerializedName("operational_city")
        val operationalCity: String,
        @SerializedName("operational_city_id")
        val operationalCityId: Int,
        @SerializedName("total_loads")
        val totalLoads: Int,
        @SerializedName("latest_loads")
        val latestLoads: List<LatestLoad>,
    )

    @Serializable
    data class LatestLoad(
        @SerializedName("indent_id")
        val indentId: String,
        val price: Double?,
        val weight: Double?,
        val material: String?,
        val priority: String?,
        @SerializedName("tat_hours")
        val tatHours: Int?,
        @SerializedName("created_at")
        val createdAt: String,
        @SerializedName("truck_type")
        val truckType: String,
        @SerializedName("tat_enabled")
        val tatEnabled: Boolean,
        @SerializedName("loading_area")
        val loadingArea: String?,
        @SerializedName("loading_city")
        val loadingCity: String?,
        @SerializedName("unloading_area")
        val unloadingArea: String?,
        @SerializedName("unloading_city")
        val unloadingCity: String?,
        @SerializedName("employee_name")
        val employeeName: String?,
        @SerializedName("employee_phone")
        val employeePhone: String?,
    )
}
