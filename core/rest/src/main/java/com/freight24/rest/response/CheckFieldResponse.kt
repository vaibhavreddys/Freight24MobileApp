package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class CheckFieldResponse(
    val exists: Boolean = false,
) : BaseResponse()
