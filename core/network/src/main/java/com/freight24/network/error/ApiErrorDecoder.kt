package com.freight24.network.error

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import retrofit2.Response

class ApiException(
    val statusCode: Int,
    val errorCode: String?,
    val details: JsonElement?,
    val rawBody: String?,
    message: String,
) : NetworkException(message)

@Serializable
private data class ApiErrorEnvelope(
    val code: String? = null,
    val message: String? = null,
    val details: JsonElement? = null,
    val data: JsonElement? = null,
    val errors: JsonElement? = null,
)

object ApiErrorDecoder {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun decode(
        statusCode: Int,
        rawBody: String?,
        bodyMessage: String? = null,
        fallbackMessage: String = "Request failed",
    ): ApiException {
        val envelope =
            rawBody
                ?.takeIf { it.isNotBlank() }
                ?.let { body -> runCatching { json.decodeFromString<ApiErrorEnvelope>(body) }.getOrNull() }
        val details =
            (envelope?.details ?: envelope?.data ?: envelope?.errors)
                ?.takeUnless { it is JsonNull }
        val message =
            envelope?.message?.takeIf { it.isNotBlank() }
                ?: bodyMessage?.takeIf { it.isNotBlank() }
                ?: fallbackMessage.takeIf { it.isNotBlank() }
                ?: "Request failed"

        return ApiException(
            statusCode = statusCode,
            errorCode = envelope?.code?.takeIf { it.isNotBlank() },
            details = details,
            rawBody = rawBody,
            message = message,
        )
    }
}

fun Response<*>.toApiException(
    bodyMessage: String? = null,
    fallbackMessage: String? = null,
): ApiException =
    ApiErrorDecoder.decode(
        statusCode = code(),
        rawBody = errorBody()?.string(),
        bodyMessage = bodyMessage,
        fallbackMessage =
            fallbackMessage?.takeIf { it.isNotBlank() }
                ?: message().takeIf { it.isNotBlank() }
                ?: "Request failed",
    )
