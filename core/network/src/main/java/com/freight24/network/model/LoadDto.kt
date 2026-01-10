package com.freight24.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoadDto(
    @SerialName("indent_id")
    val indentId: String,
    val price: Double?,
    val weight: Double?,
    val material: String?,
    val priority: String?,
    @SerialName("tat_hours")
    val tatHours: Int?,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("truck_type")
    val truckType: String,
    @SerialName("tat_enabled")
    val tatEnabled: Boolean,
    @SerialName("loading_area")
    val loadingArea: String?,
    @SerialName("loading_city")
    val loadingCity: String?,
    @SerialName("unloading_area")
    val unloadingArea: String?,
    @SerialName("unloading_city")
    val unloadingCity: String?,
    @SerialName("employee_name")
    val employeeName: String?,
    @SerialName("employee_phone")
    val employeePhone: String?,
)
