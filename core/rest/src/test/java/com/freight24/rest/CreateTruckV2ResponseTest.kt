package com.freight24.rest

import com.freight24.rest.response.CreateTruckV2Response
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class CreateTruckV2ResponseTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `decodes the v2 truck creation response`() {
        val response =
            json.decodeFromString<CreateTruckV2Response>(
                """
                {
                  "success": true,
                  "message": "Truck added successfully",
                  "data": {
                    "truckId": "8c8e7768-cdb9-4dc7-8402-2f1c5e66e024",
                    "registrationNumber": "TN01AB1234",
                    "isVerified": false,
                    "isBlacklisted": false,
                    "status": "inactive",
                    "availability": "AVAILABLE",
                    "owner": {
                      "name": "ABC Transport",
                      "phone": "+919876543210",
                      "tagCode": "F24-1234"
                    },
                    "vehicle": {
                      "make": null,
                      "model": null,
                      "year": null,
                      "capacityMt": 16.5
                    },
                    "truckType": "32 Feet Multi Axle",
                    "bodyType": "Closed",
                    "displayName": "32 ft MXL",
                    "activationEligibility": {
                      "eligible": false
                    }
                  }
                }
                """.trimIndent(),
            )

        val truck = requireNotNull(response.data)
        assertEquals("8c8e7768-cdb9-4dc7-8402-2f1c5e66e024", truck.truckId)
        assertEquals("TN01AB1234", truck.registrationNumber)
        assertEquals("inactive", truck.status)
        assertFalse(truck.isVerified)
        assertEquals("ABC Transport", truck.owner?.name)
        assertEquals("F24-1234", truck.owner?.tagCode)
        assertEquals(16.5, truck.vehicle?.capacityMt)
        assertNull(truck.vehicle?.make)
        assertEquals("32 ft MXL", truck.displayName)
    }

    @Test
    fun `decodes duplicate registration error without data`() {
        val response = json.decodeFromString<CreateTruckV2Response>(
            """{"success":false,"message":"Registration number already exists","code":"DUPLICATE_REGISTRATION"}""",
        )

        assertFalse(response.success)
        assertNull(response.data)
    }
}
