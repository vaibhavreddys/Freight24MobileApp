package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class ClientDto(
    val id: String,
    val name: String,
    val address: String? = null
)
