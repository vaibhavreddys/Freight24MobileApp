package com.freight24.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class TruckCategoryDto(
    val open: List<TruckTypeDto>,
    val closed: List<TruckTypeDto>,
    val trailer: List<TruckTypeDto>,
) : BaseResponse()

@Serializable
data class TruckTypeResponse(
    val data: TruckCategoryDto,
) : BaseResponse()
