package com.freight24.core.auth

import android.util.Log
import com.freight24.core.network.api.AuthApi
import com.freight24.core.network.model.OtpDebugInfo
import com.freight24.core.network.model.RequestOtpRequest
import com.freight24.core.network.model.VerifyOtpRequest
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val authDataStore: AuthDataStore
) {

    suspend fun requestOtp(phoneNumber: String): Result<OtpDebugInfo> {
        return try {
            val response = authApi.requestOtp(RequestOtpRequest(phoneNumber))
            if (response.success) {
                Log.e("AuthRepository", "OTP sent successfully: $response")
                    val otp = OtpExtractor.extract(response.data)
                    otp?.let {
                        Log.d("AuthRepository", "DEBUG OTP: $it")
                    }
                Result.success(OtpDebugInfo(response.data))
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyOtp(phoneNumber: String, otp: String): Result<String> {
        return try {
            Log.e("AuthRepository", "Verifying OTP: $otp : " + VerifyOtpRequest(phoneNumber, otp).toString())
            val response = authApi.verifyOtp(VerifyOtpRequest(phoneNumber, otp))
            Log.e("AuthRepository", "Response: $response")
            if (!response.success) {
                Log.e("AuthRepository", "Failed verifying OTP: " + response.message)
                return Result.failure(Exception(response.message))
            }

            val authData = response.data
                ?: return Result.failure(Exception("Missing data in response"))

            val token = authData.token
                ?: return Result.failure(Exception("Token not provided by server"))

            val userId = authData.user?.id.orEmpty()

            // Save securely
            Log.i("AuthRepository", "Saving token now")
            authDataStore.saveAuthData(token, phoneNumber, userId)

            Result.success(token)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Exception verifying OTP: " + e.message)
            Result.failure(e)
        }
    }

    suspend fun getSavedToken(): String? = authDataStore.authToken.first()
}
