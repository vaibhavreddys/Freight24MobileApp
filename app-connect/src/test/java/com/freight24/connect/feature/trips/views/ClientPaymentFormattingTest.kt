package com.freight24.connect.feature.trips.views

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class ClientPaymentFormattingTest {
    @Test
    fun `formats rupee amounts without losing paise or large integer precision`() {
        assertEquals("\u20B950,000", formatClientMoney(BigDecimal("50000")))
        assertEquals("\u20B91,000.25", formatClientMoney(BigDecimal("1000.25")))
        assertEquals("\u20B99,007,199,254,740,993.01", formatClientMoney(BigDecimal("9007199254740993.01")))
    }

    @Test
    fun `only presentation defaults missing amounts to zero`() {
        assertEquals("\u20B90", formatClientMoney(null))
    }
}
