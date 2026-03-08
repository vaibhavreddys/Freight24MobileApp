package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class ClientListResponse(
//    val success: Boolean,
//    val message: String,
    val count: Int? = null,
    val data: List<ClientDto>,
) : BaseResponse()

@Serializable
data class ClientResponse(
    val data: List<ClientDto>,
) : BaseResponse()

@Serializable
data class ClientDto(
    val clientId: String,
    val userId: String,
    val companyName: String,
    val tagCode: String,
    val phoneNumber: String,
    val status: String,
    val fullName: String,
    )
