package com.freight24.network.error

import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ApiErrorDecoderTest {
    @Test
    fun `decodes standardized v2 error fields`() {
        val exception =
            ApiErrorDecoder.decode(
                statusCode = 422,
                rawBody =
                    """
                    {
                      "success": false,
                      "code": "TRUCK_NOT_ELIGIBLE",
                      "message": "Truck cannot be activated",
                      "details": {
                        "activationEligibility": { "eligible": false }
                      }
                    }
                    """.trimIndent(),
            )

        assertEquals(422, exception.statusCode)
        assertEquals("TRUCK_NOT_ELIGIBLE", exception.errorCode)
        assertEquals("Truck cannot be activated", exception.message)
        assertEquals(
            false,
            exception.details
                ?.jsonObject
                ?.get("activationEligibility")
                ?.jsonObject
                ?.get("eligible")
                ?.jsonPrimitive
                ?.content
                ?.toBoolean(),
        )
    }

    @Test
    fun `supports legacy data and errors fields during migration`() {
        val dataError =
            ApiErrorDecoder.decode(
                statusCode = 422,
                rawBody = """{"message":"Not eligible","data":{"status":"inactive"}}""",
            )
        val validationError =
            ApiErrorDecoder.decode(
                statusCode = 400,
                rawBody = """{"message":"Invalid request","errors":[{"field":"status"}]}""",
            )

        assertEquals("inactive", dataError.details?.jsonObject?.get("status")?.jsonPrimitive?.content)
        assertEquals(
            "status",
            validationError.details?.jsonArray?.first()?.jsonObject?.get("field")?.jsonPrimitive?.content,
        )
    }

    @Test
    fun `uses supplied messages when body is empty or malformed`() {
        val bodyMessage =
            ApiErrorDecoder.decode(
                statusCode = 409,
                rawBody = null,
                bodyMessage = "Duplicate registration",
            )
        val fallback =
            ApiErrorDecoder.decode(
                statusCode = 500,
                rawBody = "not-json",
                fallbackMessage = "Could not save truck",
            )

        assertEquals("Duplicate registration", bodyMessage.message)
        assertEquals("Could not save truck", fallback.message)
        assertNull(fallback.errorCode)
        assertNull(fallback.details)
        assertEquals("not-json", fallback.rawBody)
    }
}
