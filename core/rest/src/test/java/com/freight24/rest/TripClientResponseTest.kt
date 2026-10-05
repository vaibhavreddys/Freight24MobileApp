package com.freight24.rest

import com.freight24.rest.response.TripDataResponse
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TripClientResponseTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `client decodes display name and company name from trip response`() {
        val client =
            json.decodeFromString<TripDataResponse.Client>(
                """{"id":"client-id","displayName":"Kemara Pvt Ltd","companyName":"Kemara Logistics","branch":{"name":"Hyderabad office"}}""",
            )

        assertEquals("Kemara Pvt Ltd", client.displayName)
        assertEquals("Kemara Logistics", client.companyName)
        assertNull(client.name)
    }

    @Test
    fun `older client responses still decode without display name`() {
        val client =
            json.decodeFromString<TripDataResponse.Client>(
                """{"id":"client-id","name":"Kemara","companyName":"Kemara Logistics"}""",
            )

        assertNull(client.displayName)
        assertEquals("Kemara", client.name)
        assertEquals("Kemara Logistics", client.companyName)
    }
}
