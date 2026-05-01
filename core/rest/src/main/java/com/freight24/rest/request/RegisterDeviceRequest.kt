package com.freight24.rest.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterDeviceRequest(
    @SerialName("fcm_token")
    val deviceToken: String,
    @SerialName("platform")
    val platform: String = "android",
    @SerialName("app_variant")
    val appVariant: String = "QA",
    @SerialName("device_id")
    val deviceId: String,
    @SerialName("device_model")
    val deviceModel: String = "Pixel8",
    @SerialName("os_version")
    val osVersion: String = "32",
    @SerialName("app_version")
    val appVersion: String = "1.0",
)
