package com.freight24.rest.request

import com.google.gson.annotations.SerializedName

data class RegisterDeviceRequest(
    @SerializedName("fcm_token")
    val deviceToken: String,
    @SerializedName("platform")
    val platform: String = "android",
    @SerializedName("app_variant")
    val appVariant: String = "QA",
    @SerializedName("device_id")
    val deviceId: String = "1893488-244-344",
    @SerializedName("device_model")
    val deviceModel: String = "Pixel8",
    @SerializedName("os_version")
    val osVersion: String = "32",
    @SerializedName("app_version")
    val appVersion: String = "1.0",
)
