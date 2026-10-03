package com.freight24.trucker.feature.loads.domain.mapper

import com.freight24.common.model.Load
import com.freight24.common.util.DateTimeUtils
import com.freight24.common.util.NameFormatter
import com.freight24.common.util.formatLoadWeight
import com.freight24.rest.response.LoadBoardResponse

fun LoadBoardResponse.LatestLoad.toLoad(fallbackCity: String): Load =
    Load(
        id = indentId.orEmpty(),
        sourceCity =
            loading.let {
                it.area
                ?: it.city
                ?: fallbackCity
            },
        destinationCity =
            unloading.let {
                it.area
                    ?: it.city
                    ?: "--"
            },
        material = material ?: "--",
        truckInfo = NameFormatter.shortenTruckName(truckType.displayName),
        weight = weight?.let { formatLoadWeight(it, "Tons") } ?: "--",
        price = price?.let { "₹$it" } ?: "--",
        createdTime = DateTimeUtils.formatRelativeTime(createdAt),
        agentName = employee?.name ?: "Freight24",
        agentPhone = employee?.phone ?: "--",
        isInterested = isInterested == true || interestStatus.equals("INTERESTED", ignoreCase = true),
    )
