package com.freight24.rest

import com.freight24.rest.response.ClientPaymentBreakdown
import com.freight24.rest.response.TripDataResponse
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class ClientPaymentBreakdownResponseTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `numeric and string amounts retain decimal precision`() {
        val payment =
            json.decodeFromString<ClientPaymentBreakdown>(
                """{
                "clientNetAmount": 9007199254740993.01,
                "clientPaidAmount": "50000.10",
                "clientPendingAmount": "4000.25",
                "clientCreditAmount": 0,
                "advanceReceivedAmount": "45000.05",
                "balanceReceivedAmount": 5000.05,
                "isPaymentCompleted": false
            }""",
            )

        assertEquals(BigDecimal("9007199254740993.01"), payment.clientNetAmount)
        assertEquals(BigDecimal("50000.10"), payment.clientPaidAmount)
        assertEquals(BigDecimal("4000.25"), payment.clientPendingAmount)
        assertEquals(BigDecimal("45000.05"), payment.advanceReceivedAmount)
        assertEquals(BigDecimal("5000.05"), payment.balanceReceivedAmount)
        assertFalse(payment.isPaymentCompleted)
    }

    @Test
    fun `missing and null amounts remain null in the model`() {
        val payment =
            json.decodeFromString<ClientPaymentBreakdown>(
                """{"clientNetAmount":null,"isPaymentCompleted":true}""",
            )

        assertNull(payment.clientNetAmount)
        assertNull(payment.clientPendingAmount)
        assertNull(payment.advanceReceivedAmount)
        assertTrue(payment.isPaymentCompleted)
    }

    @Test
    fun `loading charges preserve reimbursable separately from total`() {
        val charges =
            json.decodeFromString<TripDataResponse.Charges>(
                """{"total":3000,"reimbursable":"1000.25"}""",
            )

        assertEquals(3000, charges.total)
        assertEquals(BigDecimal("1000.25"), charges.reimbursable)
    }

    @Test
    fun `halting decimals and missing amounts decode correctly`() {
        val halting =
            json.decodeFromString<TripDataResponse.HaltingCharges>(
                """{"days":2,"totalAmount":2000.50,"perDayAmount":"1000.25"}""",
            )
        assertEquals(BigDecimal("2000.50"), halting.totalAmount)
        assertEquals(BigDecimal("1000.25"), halting.perDayAmount)

        val missing = json.decodeFromString<TripDataResponse.HaltingCharges>("""{"days":0}""")
        assertNull(missing.totalAmount)
        assertNull(missing.perDayAmount)
    }

    @Test
    fun `trip response exposes authoritative client payment breakdown`() {
        val payload = """{
            "id":"trip-id", "indentId":"indent-id", "tripCode":"T-1",
            "createdAt":"2026-10-04T13:38:28Z", "driverName":null, "driverPhone":null,
            "clientStatus":"ACTIVE", "indentGroupId":"group-id", "status":"ACTIVE",
            "truckerStatus":"LOADING", "clientTripCost":"50000.25",
            "truckerTripCost":null, "truckerNetAmount":null,
            "indentDetails":{
                "material":"Goods", "priority":"medium", "tatHours":null,
                "loadWeight":12, "tatEnabled":false, "flooringType":null,
                "vehicleHeight":null, "tarpaulingRequired":false,
                "truckTypeId":"12", "specialRequirements":null, "truckType":null
            },
            "client":{"id":"client-id"},
            "truck":{
                "id":"truck-id", "registrationNumber":"KA11JA1991", "truckTypeId":"12",
                "truckType":{"bodyType":"Closed","truckType":"32 Feet","displayName":"32 ft"}
            },
            "loadingLocation":{"id":"loading-id","area":null,"city":"Bengaluru"},
            "unLoadingLocation":{"id":"unloading-id","area":null,"city":"Chennai"},
            "clientPaymentBreakdown":{
                "clientNetAmount":54000.75, "clientPaidAmount":50000,
                "clientPendingAmount":4000.75, "clientCreditAmount":0,
                "advanceReceivedAmount":45000, "balanceReceivedAmount":5000
            }
        }"""
        val trip = json.decodeFromString<TripDataResponse>(payload)

        assertEquals(BigDecimal("50000.25"), trip.clientTripCost)
        assertEquals(BigDecimal("54000.75"), trip.clientPaymentBreakdown?.clientNetAmount)
        assertEquals(BigDecimal("4000.75"), trip.clientPaymentBreakdown?.clientPendingAmount)
        val absent =
            json.decodeFromString<TripDataResponse>(
                json.parseToJsonElement(payload).let { element ->
                    kotlinx.serialization.json.JsonObject(
                        (element as kotlinx.serialization.json.JsonObject) - "clientPaymentBreakdown",
                    ).toString()
                },
            )
        assertNull(absent.clientPaymentBreakdown)
    }
}
