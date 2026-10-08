package com.freight24.connect.feature.trips.domain.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DriverPhoneValidationTest {
    @Test
    fun inputIsLimitedToTenAsciiDigits() {
        assertEquals("9876543210", normalizeDriverPhoneInput("987654321012345"))
        assertEquals("9876543210", normalizeDriverPhoneInput("98765-43210 abc"))
        assertEquals("", normalizeDriverPhoneInput("abc"))
    }

    @Test
    fun pastedIndianCountryCodeIsRemoved() {
        assertEquals("9876543210", normalizeDriverPhoneInput("+91 98765 43210"))
        assertEquals("9876543210", normalizeDriverPhoneInput("919876543210"))
        assertEquals("9123456789", normalizeDriverPhoneInput("91234567890"))
    }

    @Test
    fun onlyCompleteLocalMobileNumbersAreValid() {
        listOf("9876543210", "6123456789", "7123456789", "8123456789").forEach { assertTrue(isValidDriverPhone(it)) }
        listOf("", "987654321", "98765432101", "+919876543210", "5123456789", "98765abc10").forEach {
            assertFalse(isValidDriverPhone(it))
        }
    }
}
