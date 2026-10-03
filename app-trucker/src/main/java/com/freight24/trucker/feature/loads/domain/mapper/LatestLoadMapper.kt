package com.freight24.trucker.feature.loads.domain.mapper

import com.freight24.common.model.Load
import com.freight24.common.util.DateTimeUtils
import com.freight24.common.util.NameFormatter
import com.freight24.common.util.formatLoadWeight
import com.freight24.rest.response.LoadBoardLoad

/**
 * Shared by the collapsed board (`load-board-summary`, rows nested under a city
 * group) and the expanded feed (`load-board-paginated`) — both return
 * [LoadBoardLoad]. [fallbackCity] is the group name, used when the load itself
 * carries no loading area or city.
 */
fun LoadBoardLoad.toLoad(fallbackCity: String): Load =
    Load(
        id = indentId.orEmpty(),
        sourceCity =
            loading?.area
                ?: loading?.city
                ?: operationalCity
                ?: fallbackCity,
        destinationCity =
            unloading?.area
                ?: unloading?.city
                ?: "--",
        material = material ?: "--",
        weight = weight?.let { formatLoadWeight(it, "Tons") } ?: "--",
        truckInfo = NameFormatter.shortenTruckName(truckType?.displayName),
        price = price?.let { "₹$it" } ?: "--",
        createdTime = DateTimeUtils.formatRelativeTime(createdAt),
        agentName = employee?.name ?: "Freight24",
        agentPhone = employee?.phone ?: "--",
        isInterested = isInterested == true || interestStatus.equals("INTERESTED", ignoreCase = true),
    )
