package com.freight24.rest

import com.freight24.rest.response.TruckOwnerOnboardingV2Response
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TruckOwnerOnboardingV2ResponseTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `decodes truck owner onboarding response and returned user id`() {
        val response =
            json.decodeFromString<TruckOwnerOnboardingV2Response>(
                """
                {
                  "success": true,
                  "message": "Truck owner created successfully",
                  "nextStep": "BUSINESS_DETAILS",
                  "data": {
                    "id": "64676b1e-1b41-4f84-958b-461760f4dcc7",
                    "profileId": "2c920275-0a87-4742-b726-e0f060f7d7ad",
                    "tagCode": "F24-1234",
                    "fullName": "ABC Transport",
                    "status": "inactive",
                    "documents": { "mandatory": [] },
                    "truckStats": { "total": 0 }
                  }
                }
                """.trimIndent(),
            )

        assertTrue(response.success)
        assertEquals("BUSINESS_DETAILS", response.nextStep)
        assertEquals("64676b1e-1b41-4f84-958b-461760f4dcc7", response.data?.id)
        assertEquals("F24-1234", response.data?.tagCode)
    }

    @Test
    fun `decodes resumable duplicate response`() {
        val response = json.decodeFromString<TruckOwnerOnboardingV2Response>(
            """
            {
              "success": true,
              "message": "Existing incomplete truck owner found; resume onboarding",
              "resumed": true,
              "nextStep": "BUSINESS_DETAILS",
              "data": { "id": "owner-user-1", "status": "inactive" }
            }
            """.trimIndent(),
        )

        assertTrue(response.resumed)
        assertEquals("owner-user-1", response.data?.id)
    }

    @Test
    fun `decodes standardized validation response`() {
        val response = json.decodeFromString<TruckOwnerOnboardingV2Response>(
            """{"success":false,"code":"TRUCK_OWNER_IDENTITY_DOCUMENTS_REQUIRED","message":"Aadhaar Card and PAN Card uploads are required"}""",
        )

        assertEquals(false, response.success)
        assertEquals(null, response.data)
    }
}
