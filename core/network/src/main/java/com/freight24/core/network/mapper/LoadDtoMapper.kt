package com.freight24.core.network.mapper

import com.freight24.core.common.model.Load
import com.freight24.core.common.util.DateTimeUtils
import com.freight24.core.common.util.TruckNameFormatter
import com.freight24.core.network.model.LoadDto

fun LoadDto.toDomain(fallbackCity: String): Load =
    Load(
        id = indentId,
        sourceCity = loadingArea
            ?: loadingCity
            ?: fallbackCity,
        destinationCity = unloadingArea
            ?: unloadingCity
            ?: "--",
        material = material ?: "--",
        weight = weight?.let { "$it Tons" } ?: "--",
        truckInfo = TruckNameFormatter.shortenTruckName(truckType),
        price = price?.let { "₹$it" } ?: "--",
        createdTime = DateTimeUtils.formatRelativeTime(createdAt),
        agentName = employeeName ?: "Freight24",
        agentPhone = employeePhone ?: "--"
    )