package com.freight24.trucker.feature.trips.view.details

import com.freight24.common.model.StatusEvent
import com.freight24.ui.trucker.tripStageIndex

internal fun tripLogEvents(events: List<StatusEvent>): List<StatusEvent> =
    events.filter {
        it.eventType.equals("trucker_status_change", ignoreCase = true) &&
            tripStageIndex(it.newStatus.orEmpty()) >= 0 &&
            tripStageIndex(it.oldStatus.orEmpty()) != tripStageIndex(it.newStatus.orEmpty())
    }

internal fun tripLogStageIndex(status: String, events: List<StatusEvent>): Int =
    tripStageIndex(status).takeIf { it >= 0 }
        ?: (events.maxOfOrNull { tripStageIndex(it.newStatus.orEmpty()) } ?: -1)
