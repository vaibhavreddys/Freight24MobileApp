package com.freight24.core.network.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.freight24.core.common.model.Load
import com.freight24.core.common.util.DateTimeUtils
import com.freight24.core.common.util.TruckNameFormatter
import com.freight24.core.network.model.LoadDto

@RequiresApi(Build.VERSION_CODES.O)
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