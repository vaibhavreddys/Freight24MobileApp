package com.freight24.rest.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
open class BaseResponse {
    @SerialName("success")
    val success: Boolean = false

    @SerialName("message")
    val message: String = ""
}
