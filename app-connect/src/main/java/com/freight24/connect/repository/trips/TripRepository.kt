package com.freight24.connect.repository.trips

import com.freight24.rest.response.BaseResponse
import com.freight24.rest.response.CreatedTripResponse
import com.freight24.rest.response.MyTripsDataResponse
import com.freight24.rest.response.SignedDocument
import com.freight24.rest.response.TripDataResponse
import com.freight24.rest.response.UpdateOtherDeductionsResponse
import com.freight24.rest.response.UpdateTripResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface TripRepository {

    suspend fun createTrip(
        indentId: String,
        truckId: String,
        driverName: String?,
        driverPhone: String?): Result<CreatedTripResponse.TripData>

    suspend fun getMyTrips(
        filter: String,
        page: Int,
        limit: Int
    ): Result<MyTripsDataResponse>

    suspend fun getTripDetails(
        tripId: String,
    ): Result<TripDataResponse>

    suspend fun updateTruckerStatus(
        tripId: String,
        truckerStatus: String,
        remarks: String?
    ): Result<TripDataResponse>

    suspend fun uploadLr(
        tripId: String,
        file: MultipartBody.Part,
       description: RequestBody? = null
    ): Result<TripDataResponse>

    suspend fun getSignedDocument(
        tripId: String,
        documentId: String,
    ): Result<SignedDocument>

    suspend fun verifyLr(
        tripId: String,
        status: String,
        remarks: String?
    ): Result<TripDataResponse>

    suspend fun updateOtherDeductions(
        tripId: String,
        amount: String,
        notes: String?
    ): Result<UpdateOtherDeductionsResponse>

    suspend fun updateTrip(
       tripId: String,
       driverName: String?,
       driverPhone: String?,
       clientTripCost: String?,
       truckerTripCost: String?,
       luIncluded: Boolean?,
       truckId: String?,
       reason: String?,
    ): Result<TripDataResponse>

    suspend fun cancelTrip(
        tripId: String,
        reason: String,
    ): Result<BaseResponse>
}