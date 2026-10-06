package com.freight24.rest

import com.freight24.rest.response.ClientOnboardingV2Response
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClientOnboardingV2ResponseTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `decodes profile response with client and user identifiers`() {
        val response = json.decodeFromString<ClientOnboardingV2Response>(
            """
            {
              "success": true,
              "message": "Client created successfully",
              "nextStep": "BRANCHES",
              "data": {
                "clientId": "2c920275-0a87-4742-b726-e0f060f7d7ad",
                "userId": "64676b1e-1b41-4f84-958b-461760f4dcc7",
                "clientType": "DIRECT_CUSTOMER",
                "companyName": "ABC Logistics",
                "status": "inactive"
              }
            }
            """.trimIndent(),
        )

        assertTrue(response.success)
        assertEquals("2c920275-0a87-4742-b726-e0f060f7d7ad", response.data?.clientId)
        assertEquals("64676b1e-1b41-4f84-958b-461760f4dcc7", response.data?.userId)
    }

    @Test
    fun `decodes location response with created branches`() {
        val response = json.decodeFromString<ClientOnboardingV2Response>(
            """
            {
              "success": true,
              "data": {
                "clientId": "2c920275-0a87-4742-b726-e0f060f7d7ad",
                "operatingStructure": "MULTIPLE",
                "branchCount": 1,
                "branches": [{
                  "id": "43c93da9-34a6-49b2-beea-2ee358ffb5c9",
                  "branchName": "Chennai",
                  "branchCode": "CHE",
                  "branchTypeId": "53c93da9-34a6-49b2-beea-2ee358ffb5c9",
                  "status": "active"
                }]
              }
            }
            """.trimIndent(),
        )

        assertEquals(1, response.data?.branchCount)
        assertEquals("Chennai", response.data?.branches?.single()?.branchName)
    }

    @Test
    fun `decodes client validation error without data`() {
        val response = json.decodeFromString<ClientOnboardingV2Response>(
            """{"success":false,"message":"PAN number already exists","code":"DUPLICATE_PAN"}""",
        )

        assertEquals(false, response.success)
        assertEquals(null, response.data)
    }
}
