package com.freight24.trucker.feature.loads.mapper

import com.freight24.common.model.Load
import com.freight24.database.entity.LoadEntity

fun Load.toEntity(
    cityId: Int,
    page: Int = 1,
): LoadEntity =
    LoadEntity(
        id = id,
        cityId = cityId,
        sourceCity = sourceCity,
        destinationCity = destinationCity,
        material = material,
        weight = weight,
        truckInfo = truckInfo,
        price = price,
        createdTime = createdTime,
        agentName = agentName,
        agentPhone = agentPhone,
        page = page,
    )

fun LoadEntity.toDomain(): Load =
    Load(
        id = id,
        sourceCity = sourceCity,
        destinationCity = destinationCity,
        material = material,
        weight = weight,
        truckInfo = truckInfo,
        price = price,
        createdTime = createdTime,
        agentName = agentName,
        agentPhone = agentPhone,
    )
