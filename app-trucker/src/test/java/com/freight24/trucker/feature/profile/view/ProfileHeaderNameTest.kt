package com.freight24.trucker.feature.profile.view

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileHeaderNameTest {
    @Test
    fun companyNameTakesPriority() {
        assertEquals("Veeksha Transports", profileHeaderName(" Veeksha Transports ", "Veeksha"))
    }

    @Test
    fun missingOrBlankCompanyNameFallsBackToTruckerName() {
        listOf(null, "", " ").forEach {
            assertEquals("Veeksha", profileHeaderName(it, " Veeksha "))
        }
    }

    @Test
    fun bothMissingRetainsMemberFallback() {
        assertEquals("Member", profileHeaderName(null, " "))
    }
}
