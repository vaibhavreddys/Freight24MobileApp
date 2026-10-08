package com.freight24.connect.feature.truckowners.domain.usecases

import com.freight24.common.util.DocumentUploadException
import com.freight24.common.util.DocumentUploadPolicy
import com.freight24.connect.repository.TruckOwnerRepository
import com.freight24.rest.request.PreferredRouteEntry
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File
import javax.inject.Inject

data class TruckOwnerPersonalParams(
    val fullName: String,
    val email: String?,
    val phone: String,
    val alternativePhone: String?,
    val panCard: String,
    val aadhaarCard: String,
    val aadhaarFile: File,
    val panFile: File,
)

data class TruckOwnerBusinessParams(
    val businessName: String?,
    val gstNumber: String?,
    val isTdsApplicable: Boolean,
    val baseCityId: Int?,
    val preferredRoutes: List<PreferredRouteEntry>,
    val gstFile: File?,
    val companyFile: File?,
    val tdsFile: File?,
)

data class TruckOwnerBankParams(
    val accountHolderName: String,
    val accountNumber: String,
    val ifscCode: String,
    val upiId: String?,
    val bankName: String?,
    val branchName: String?,
    val chequeFile: File?,
)

interface CreateTruckOwnerUseCase {
    suspend fun createPersonal(params: TruckOwnerPersonalParams): Result<String>

    suspend fun saveBusiness(ownerUserId: String, params: TruckOwnerBusinessParams): Result<Unit>

    suspend fun saveBank(ownerUserId: String, params: TruckOwnerBankParams): Result<Unit>
}

class CreateTruckOwnerUseCaseImpl
    @Inject
    constructor(
        private val repository: TruckOwnerRepository,
    ) : CreateTruckOwnerUseCase {
        override suspend fun createPersonal(params: TruckOwnerPersonalParams): Result<String> {
            try {
                val parts = buildList {
                    add(params.fullName.toPart("full_name"))
                    params.email?.let { add(it.toPart("email")) }
                    add(params.phone.toPart("phone"))
                    params.alternativePhone?.let { add(it.toPart("alt_phones")) }
                    add(params.panCard.toPart("pan_card"))
                    add(params.aadhaarCard.toPart("aadhar_card"))
                    add(params.aadhaarFile.toFilePart("aadhaar_file"))
                    add(params.panFile.toFilePart("pan_file"))
                }
                return repository.createPersonalProfile(parts).mapCatching {
                    it.data?.id ?: error("Truck owner creation returned no user ID")
                }
            } catch (error: DocumentUploadException) {
                return Result.failure(error)
            }
        }

        override suspend fun saveBusiness(ownerUserId: String, params: TruckOwnerBusinessParams): Result<Unit> {
            try {
                val routes = buildJsonArray {
                    params.preferredRoutes.forEach { route ->
                        add(buildJsonObject {
                            put("source_city_id", route.sourceCityId)
                            put("destination_city_id", route.destinationCityId)
                            put("priority", route.priority)
                        })
                    }
                }.toString()
                val parts = buildList {
                    params.businessName?.let { add(it.toPart("business_name")) }
                    params.gstNumber?.let { add(it.toPart("gst_number")) }
                    add(params.isTdsApplicable.toString().toPart("is_tds_applicable"))
                    params.baseCityId?.let { add(it.toString().toPart("base_city_id")) }
                    add(routes.toPart("preferred_routes"))
                    params.gstFile?.let { add(it.toFilePart("gst_file")) }
                    params.companyFile?.let { add(it.toFilePart("company_file")) }
                    params.tdsFile?.let { add(it.toFilePart("tds_file")) }
                }
                return repository.updateBusinessProfile(ownerUserId, parts).map { Unit }
            } catch (error: DocumentUploadException) {
                return Result.failure(error)
            }
        }

        override suspend fun saveBank(ownerUserId: String, params: TruckOwnerBankParams): Result<Unit> {
            try {
                val parts = buildList {
                    add(params.accountHolderName.toPart("account_holder_name"))
                    add(params.accountNumber.toPart("account_number"))
                    add(params.ifscCode.toPart("ifsc_code"))
                    params.upiId?.let { add(it.toPart("upi_id")) }
                    params.bankName?.let { add(it.toPart("bank_name")) }
                    params.branchName?.let { add(it.toPart("branch_name")) }
                    params.chequeFile?.let { add(it.toFilePart("cheque_file")) }
                }
                return repository.updateBankProfile(ownerUserId, parts).map { Unit }
            } catch (error: DocumentUploadException) {
                return Result.failure(error)
            }
        }

        private fun String.toPart(name: String): MultipartBody.Part =
            MultipartBody.Part.createFormData(name, this)

        private fun File.toFilePart(name: String): MultipartBody.Part {
            val mimeType = if (extension.equals("pdf", true)) "application/pdf" else "application/octet-stream"
            DocumentUploadPolicy.validateFile(this)
            return MultipartBody.Part.createFormData(name, this.name, asRequestBody(mimeType.toMediaTypeOrNull()))
        }
    }
