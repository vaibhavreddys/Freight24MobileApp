package com.freight24.connect.feature.truckowners.domain.viewmodel

import com.freight24.connect.feature.truckowners.domain.usecases.TruckOwnerBankParams
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BankingValidationTest {
    private val emptyBank = TruckOwnerBankParams("", "", "", null, null, null, null)
    private val validBank = emptyBank.copy(accountHolderName = "ABC Transport", accountNumber = "123456789", ifscCode = "HDFC0001234")

    @Test
    fun emptyBankingIsOptional() {
        assertFalse(emptyBank.hasDetails())
        assertNull(emptyBank.validationError())
        assertFalse(emptyBank.copy(accountHolderName = " ", bankName = " ").hasDetails())
    }

    @Test
    fun completeBankingDoesNotRequireCheque() {
        assertTrue(validBank.hasDetails())
        assertNull(validBank.validationError())
    }

    @Test
    fun partialBankingRequiresAccountDetails() {
        listOf(
            emptyBank.copy(accountHolderName = "ABC"),
            emptyBank.copy(upiId = "owner@upi"),
            emptyBank.copy(chequeFile = File("cheque.pdf")),
            validBank.copy(accountNumber = ""),
            validBank.copy(ifscCode = ""),
        ).forEach { bank ->
            assertTrue(bank.hasDetails())
            assertNotNull(bank.validationError())
        }
    }

    @Test
    fun invalidAccountIfscAndUpiAreRejected() {
        assertNotNull(validBank.copy(accountNumber = "123").validationError())
        assertNotNull(validBank.copy(ifscCode = "BAD").validationError())
        assertNotNull(validBank.copy(upiId = "invalid").validationError())
    }
}
