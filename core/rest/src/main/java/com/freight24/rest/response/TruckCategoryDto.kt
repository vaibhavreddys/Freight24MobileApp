package com.freight24.rest.response

data class TruckCategoryDto(
    val open: List<TruckTypeDto>,
    val closed: List<TruckTypeDto>,
    val trailer: List<TruckTypeDto>,
) : BaseResponse()

data class TruckTypeResponse(
    val data: TruckCategoryDto,
) : BaseResponse()
