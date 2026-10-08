package com.freight24.common.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TripLocationTest {
    @Test
    fun areaTakesPriorityOverCity() {
        assertEquals("Hoskote", TripLocation("Bengaluru", " Hoskote ", "Address").toDisplayString())
    }

    @Test
    fun missingOrBlankAreaFallsBackToCity() {
        listOf(null, "", " ").forEach { area ->
            assertEquals("Bengaluru", TripLocation(" Bengaluru ", area, "Address").toDisplayString())
        }
    }

    @Test
    fun addressIsUsedWhenAreaAndCityAreUnavailable() {
        assertEquals("Address", TripLocation(null, " ", " Address ").toDisplayString())
    }

    @Test
    fun unavailableLocationShowsUnknown() {
        assertEquals("Unknown", TripLocation(null, null, null).toDisplayString())
        assertEquals("Unknown", TripLocation(" ", "", " ").toDisplayString())
    }
}
