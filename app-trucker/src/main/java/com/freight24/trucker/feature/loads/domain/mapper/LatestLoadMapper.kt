package com.freight24.trucker.feature.loads.domain.mapper

import com.freight24.common.model.Load
import com.freight24.common.util.DateTimeUtils
import com.freight24.common.util.NameFormatter
import com.freight24.rest.response.LoadBoardResponse

fun LoadBoardResponse.LatestLoad.toLoad(fallbackCity: String): Load =
    Load(
        id = indentId,
        sourceCity =
            loadingArea
                ?: loadingCity
                ?: fallbackCity,
        destinationCity =
            unloadingArea
                ?: unloadingCity
                ?: "--",
        material = material ?: "--",
        weight = weight?.let { "$it Tons" } ?: "--",
        truckInfo = NameFormatter.shortenTruckName(truckType),
        price = price?.let { "₹$it" } ?: "--",
        createdTime = DateTimeUtils.formatRelativeTime(createdAt),
        agentName = employeeName ?: "Freight24",
        agentPhone = employeePhone ?: "--",
    )
