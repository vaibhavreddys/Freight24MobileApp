package com.freight24.connect.repository.trips

import com.freight24.common.di.IoDispatcher
import com.freight24.rest.api.TripApi
import com.freight24.rest.request.CancelTripRequest
import com.freight24.rest.request.CreateTripRequest
import com.freight24.rest.request.LrVerifyRequest
import com.freight24.rest.request.UpdateOtherDeductionsRequest
import com.freight24.rest.request.UpdateTripRequest
import com.freight24.rest.request.UpdateTruckerStatus
import com.freight24.rest.response.BaseResponse
import com.freight24.rest.response.CreatedTripResponse
import com.freight24.rest.response.MyTripsDataResponse
import com.freight24.rest.response.PaymentTransaction
import com.freight24.rest.response.SignedDocument
import com.freight24.rest.response.TripDataResponse
import com.freight24.rest.response.UpdateOtherDeductionsResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class TripRepositoryImpl @Inject constructor(
    private val api: TripApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : TripRepository {

    override suspend fun createTrip(
        indentId: String,
        truckId: String,
        driverName: String?,
        driverPhone: String?
    ): Result<CreatedTripResponse.TripData> =
        withContext(ioDispatcher) {
            try {
                val response = api.createTrip(
                    CreateTripRequest(
                        indentId,
                        truckId,
                        driverName,
                        driverPhone
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.data)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getMyTrips(
        filter: String?,
        page: Int,
        limit: Int
    ): Result<MyTripsDataResponse> =
        withContext(ioDispatcher) {
            try {
                val response = api.getEmpMyTrips(
                    filter,
                    page,
                    limit
                )
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getTripDetails(
        tripId: String
    ): Result<TripDataResponse> =
        withContext(ioDispatcher) {
            try {
                val response = api.getEmpTripDetails(tripId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.data)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getPaymentTransactions(
        tripId: String,
        userType: String
    ): Result<List<PaymentTransaction>> =
        withContext(ioDispatcher) {
            try {
                val response = api.getPaymentTransactions(tripId, userType)
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun updateTruckerStatus(
        tripId: String,
        truckerStatus: String,
        remarks: String?
    ): Result<TripDataResponse> =
        withContext(ioDispatcher) {
            try {
                val response = api.updateTripStatus(
                    tripId,
                    UpdateTruckerStatus(
                        truckerStatus = truckerStatus,
                        remarks = remarks
                    )
                )
                if (response.isSuccessful) {
                    getTripDetails(tripId)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun uploadLr(
        tripId: String,
        file: MultipartBody.Part,
        description: RequestBody?
    ): Result<TripDataResponse> =
        withContext(ioDispatcher) {
            try {
                val response = api.uploadLr(
                    tripId,
                    file,
                    description
                )
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.data.trip!!)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getSignedDocument(
        tripId: String,
        documentId: String
    ): Result<SignedDocument> =
        withContext(ioDispatcher) {
            try {
                val response = api.getSignedDocument(
                    tripId,
                    documentId,
                )
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun verifyLr(
        tripId: String,
        status: String,
        remarks: String?
    ): Result<TripDataResponse> =
        withContext(ioDispatcher) {
            try {
                val response = api.verifyLr(
                    tripId = tripId,
                    lrVerifyRequest = LrVerifyRequest(
                        status = status,
                        remarks = remarks
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.data)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun updateOtherDeductions(
        tripId: String,
        amount: String,
        notes: String?
    ): Result<UpdateOtherDeductionsResponse> =
        withContext(ioDispatcher) {
            try {
                val response = api.updateOtherDeductions(
                    tripId = tripId,
                    updateOtherDeductionsRequest = UpdateOtherDeductionsRequest(
                        amount = amount,
                        remarks = notes
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun updateTrip(
        tripId: String,
        driverName: String?,
        driverPhone: String?,
        clientTripCost: String?,
        truckerTripCost: String?,
        luIncluded: Boolean?,
        truckId: String?,
        reason: String?
    ): Result<TripDataResponse> =
        withContext(ioDispatcher) {
            try {
                val response = api.updateTrip(
                    tripId = tripId,
                    updateTripRequest = UpdateTripRequest(
                        driverName = driverName,
                        driverPhone = driverPhone,
                        clientTripCost = clientTripCost,
                        truckerTripCost = truckerTripCost,
                        luIncluded = luIncluded,
                        truckId = truckId,
                        reason = reason
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.data)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun cancelTrip(
        tripId: String,
        reason: String,
    ): Result<BaseResponse> =
        withContext(ioDispatcher) {
            try {
                val response = api.cancelTrip(
                    tripId = tripId,
                    cancelTripRequest = CancelTripRequest(
                        reason = reason
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
