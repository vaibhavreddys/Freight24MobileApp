package com.freight24.trucker.feature.loads.mapper

import com.freight24.core.common.model.Load
import com.freight24.trucker.feature.loads.model.ui.LoadUi

fun Load.toUi(): LoadUi =
    LoadUi(
        id = id,
        sourceCity = sourceCity,
        destinationCity = destinationCity,
        postedTime = createdTime,
        price = price,
        truckInfo = truckInfo,
        loadWeight = weight,
        commodity = material,
        f24AgentName = agentName,
        f24AgentPhone = agentPhone
    )
