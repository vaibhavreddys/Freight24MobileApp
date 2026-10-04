package com.freight24.rest

import com.freight24.rest.response.PaymentTransactionsResponse
import java.math.BigDecimal
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class PaymentTransactionResponseTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `transaction response preserves amount and verifier details`() {
        val response = json.decodeFromString<PaymentTransactionsResponse>(
            """{
                "success":true,
                "data":[{
                    "id":"payment-id",
                    "amount":9007199254740993.01,
                    "paymentMethod":"bank_transfer",
                    "paymentDate":"2026-10-04T13:38:28Z",
                    "verifiedBy":{"id":"user-id","name":"Finance User"}
                }]
            }"""
        )

        assertEquals(BigDecimal("9007199254740993.01"), response.data.single().amount)
        assertEquals("Finance User", response.data.single().verifiedBy?.name)
    }
}
