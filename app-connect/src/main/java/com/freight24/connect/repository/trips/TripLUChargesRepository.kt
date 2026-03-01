package com.freight24.connect.repository.trips

import com.freight24.connect.feature.trips.domain.model.LuChargeRequest
import com.freight24.rest.response.SignedDocument
import com.freight24.rest.response.UpdatedLUResponse
import retrofit2.Response
import retrofit2.http.Path
import java.io.File

interface TripLUChargesRepository {

    suspend fun updateLUCharges(
        tripId: String,
        loadingFile: File? = null,
        unloadingFile: File? = null,
        request: LuChargeRequest
    ): Result<UpdatedLUResponse>

    suspend fun getSignedLUDocument(
        tripId: String,
        chargeId: String,
    ): Result<SignedDocument>

}