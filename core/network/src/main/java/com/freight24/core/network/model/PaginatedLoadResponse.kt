package com.freight24.core.network.model

import kotlinx.serialization.Serializable


@Serializable
data class PaginatedLoadResponse(
    val success: Boolean,
    val data: PaginatedLoadData?
)

@Serializable
data class PaginatedLoadData(
    val loads: List<LoadDto>,
    val pagination: PaginationDto
)

@Serializable
data class PaginationDto(
    val page: Int,
    val limit: Int,
    val total_loads: Int,
    val has_more: Boolean
)
