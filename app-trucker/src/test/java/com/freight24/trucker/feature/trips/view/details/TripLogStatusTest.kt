package com.freight24.trucker.feature.trips.view.details

import com.freight24.common.model.StatusEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class TripLogStatusTest {
    @Test
    fun blankPaidDescriptionDoesNotHidePaidCheckpoint() {
        val events = tripLogEvents(listOf(event("INVOICED", "Billed"), event("PAID", "")))
        assertEquals(2, events.size)
        assertEquals("PAID", events.last().newStatus)
        assertEquals(5, tripLogStageIndex("PAID", events))
    }

    @Test
    fun nullDescriptionsAreAlsoValidCheckpoints() {
        assertEquals(1, tripLogEvents(listOf(event("PAID", null))).size)
    }

    @Test
    fun currentStatusWinsOverMissingOrOlderEvents() {
        assertEquals(5, tripLogStageIndex("Paid", listOf(event("INVOICED", "Billed"))))
        assertEquals(5, tripLogStageIndex("PAID", emptyList()))
    }

    @Test
    fun unknownCurrentStatusFallsBackToLatestKnownCheckpoint() {
        assertEquals(5, tripLogStageIndex("unknown", tripLogEvents(listOf(event("PAID", "")))))
        assertEquals(-1, tripLogStageIndex("unknown", emptyList()))
    }

    @Test
    fun unrelatedUnknownAndUnchangedEventsAreIgnored() {
        assertEquals(
            emptyList<StatusEvent>(),
            tripLogEvents(listOf(
                event("PAID", "").copy(eventType = "client_status_change"),
                event("unknown", "Something changed"),
                event("PAID", "").copy(oldStatus = "paid"),
            )),
        )
    }

    private fun event(status: String, details: String?) = StatusEvent(
        eventType = "trucker_status_change",
        eventBy = "employee",
        eventAt = "2026-10-08T08:39:39.793408+00:00",
        details = details,
        newStatus = status,
    )
}
