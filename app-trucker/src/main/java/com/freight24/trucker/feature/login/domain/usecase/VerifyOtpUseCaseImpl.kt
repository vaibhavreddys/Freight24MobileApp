package com.freight24.trucker.feature.login.domain.usecase

import com.freight24.rest.response.LoginResponse
import com.freight24.trucker.feature.login.data.models.VerifyOtpResult
import com.freight24.trucker.feature.login.data.repository.LoginRepository
import com.freight24.trucker.feature.login.domain.AuthErrorMessage
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

private val errorJson = Json { ignoreUnknownKeys = true }

@Singleton
internal class VerifyOtpUseCaseImpl
    @Inject
    constructor(
        private val loginRepository: LoginRepository,
    ) : VerifyOtpUseCase {
        override suspend fun invoke(params: VerifyOtpUseCaseParams): Result<VerifyOtpResult> =
            runCatching {
                val response = loginRepository.verifyOtp(params.phoneNumber, params.otp)

                if (!response.success) {
                    val message = response.message
                    return@runCatching when {
                        message.contains("Invalid", ignoreCase = true) ->
                            VerifyOtpResult.InvalidOtp(message)
                        message.contains("expired", ignoreCase = true) ->
                            VerifyOtpResult.OtpExpired(message)
                        else -> VerifyOtpResult.Error(message)
                    }
                }

                val authData =
                    response.data
                        ?: return@runCatching VerifyOtpResult.OtpExpired("Missing data in response")

                val token =
                    authData.access_token
                        ?: authData.token
                        ?: return@runCatching VerifyOtpResult.InvalidOtp("Token not provided by server")

                val userId = authData.user?.id.orEmpty()
                VerifyOtpResult.Success(true, token)
            }.recover { e ->
                if (e is HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    if (errorBody != null) {
                        try {
                            val errorResponse = errorJson
                                .decodeFromString<LoginResponse>(errorBody)
                            // Server-authored copy is fine to show, but a body with no
                            // message would render an empty error block.
                            val message = AuthErrorMessage.orGeneric(errorResponse.message)
                            return@recover when {
                                message.contains("Invalid", ignoreCase = true) ->
                                    VerifyOtpResult.InvalidOtp(message)
                                message.contains("expired", ignoreCase = true) ->
                                    VerifyOtpResult.OtpExpired(message)
                                else -> VerifyOtpResult.Error(message)
                            }
                        } catch (_: Exception) {
                            // parsing failed - fall through
                        }
                    }
                }
                // Transport failure. e.message is the raw OkHttp/Retrofit
                // string and used to reach the screen verbatim — the OTP page
                // was printing "Failed to connect to <our base URL>" at the
                // driver. Route it through the mapper instead.
                VerifyOtpResult.Error(AuthErrorMessage.from(e))
            }
    }
