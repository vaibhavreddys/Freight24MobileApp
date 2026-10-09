package com.freight24.trucker.feature.profile.view

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IfscLookupValidationTest {
    @Test
    fun completeValidCodeTriggersLookup() {
        assertEquals("HDFC0001234", ifscLookupCode("HDFC0001234"))
        assertEquals("HDFC0001234", ifscLookupCode(" hdfc0001234 "))
    }

    @Test
    fun incompleteCodesNeverTriggerLookup() {
        (0..10).forEach { assertNull(ifscLookupCode("HDFC0001234".take(it))) }
    }

    @Test
    fun invalidAndOverlongCodesDoNotTriggerLookup() {
        listOf("HDFC1001234", "12340001234", "HDFC00012345", "HDFC000123!").forEach {
            assertNull(ifscLookupCode(it))
        }
    }
}
