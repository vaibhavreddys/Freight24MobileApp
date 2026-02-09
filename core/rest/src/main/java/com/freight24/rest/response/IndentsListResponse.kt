package com.freight24.rest.response

import com.freight24.rest.response.IndentResponse.Indent
import kotlinx.serialization.Serializable

@Serializable
data class IndentsListResponse(
    val data: List<Indent>
): BaseResponse()
