package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class GetIndentsResponse(
    val page: Int,
    val limit: Int,
    val total: Int,
    val data: List<IndentResponse.Indent>
): BaseResponse()
