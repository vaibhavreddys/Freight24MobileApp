package com.freight24.connect.feature.truckowners.profile.domain.usecase

import com.freight24.connect.repository.TruckOwnerRepository
import com.freight24.rest.request.PreferredRouteEntry
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

enum class TruckOwnerEditSection(val label: String) {
    PERSONAL("Personal details"),
    BUSINESS("Business details"),
    BANK("Bank details"),
}

data class TruckOwnerPersonalEditParams(
    val fullName: String,
    val email: String?,
    val phone: String,
    val alternativePhones: List<String>,
    val panCard: String,
    val aadhaarCard: String,
    val aadhaarFile: File?,
    val panFile: File?,
)

data class TruckOwnerBusinessEditParams(
    val businessName: String?,
    val gstNumber: String?,
    val isTdsApplicable: Boolean,
    val baseCityId: Int?,
    val baseAreaId: Int?,
    val preferredRoutes: List<PreferredRouteEntry>,
    val gstFile: File?,
    val companyFile: File?,
    val tdsFile: File?,
)

data class TruckOwnerBankEditParams(
    val accountHolderName: String,
    val accountNumber: String,
    val ifscCode: String,
    val upiId: String?,
    val bankName: String?,
    val branchName: String?,
    val chequeFile: File?,
)

data class TruckOwnerEditRequest(
    val ownerUserId: String,
    val personal: TruckOwnerPersonalEditParams? = null,
    val business: TruckOwnerBusinessEditParams? = null,
    val bank: TruckOwnerBankEditParams? = null,
)

class TruckOwnerEditException(
    val failedSection: TruckOwnerEditSection,
    val completedSections: Set<TruckOwnerEditSection>,
    cause: Throwable,
) : RuntimeException("${failedSection.label}: ${cause.message ?: "update failed"}", cause)

interface UpdateTruckOwnerSectionsUseCase {
    suspend operator fun invoke(
        request: TruckOwnerEditRequest,
        completedSections: Set<TruckOwnerEditSection> = emptySet(),
    ): Result<Set<TruckOwnerEditSection>>
}

class UpdateTruckOwnerSectionsUseCaseImpl
    @Inject
    constructor(
        private val repository: TruckOwnerRepository,
    ) : UpdateTruckOwnerSectionsUseCase {
        override suspend fun invoke(
            request: TruckOwnerEditRequest,
            completedSections: Set<TruckOwnerEditSection>,
        ): Result<Set<TruckOwnerEditSection>> {
            val completed = completedSections.toMutableSet()
            val operations =
                listOfNotNull(
                    request.personal?.let { params ->
                        TruckOwnerEditSection.PERSONAL to suspend {
                            repository.updatePersonalProfile(request.ownerUserId, params.toParts()).map { Unit }
                        }
                    },
                    request.business?.let { params ->
                        TruckOwnerEditSection.BUSINESS to suspend {
                            repository.updateBusinessProfile(request.ownerUserId, params.toParts()).map { Unit }
                        }
                    },
                    request.bank?.let { params ->
                        TruckOwnerEditSection.BANK to suspend {
                            repository.updateBankProfile(request.ownerUserId, params.toParts()).map { Unit }
                        }
                    },
                )

            operations.forEach { (section, operation) ->
                if (section in completed) return@forEach
                val result = operation()
                if (result.isFailure) {
                    return Result.failure(
                        TruckOwnerEditException(
                            failedSection = section,
                            completedSections = completed.toSet(),
                            cause = result.exceptionOrNull() ?: IllegalStateException("Update failed"),
                        ),
                    )
                }
                completed += section
            }

            return Result.success(completed)
        }

        private fun TruckOwnerPersonalEditParams.toParts() = buildList {
            add(fullName.toPart("full_name"))
            email?.let { add(it.toPart("email")) }
            add(phone.toPart("phone"))
            alternativePhones.takeIf { it.isNotEmpty() }?.let { add(it.joinToString(",").toPart("alt_phones")) }
            add(panCard.toPart("pan_card"))
            add(aadhaarCard.toPart("aadhar_card"))
            aadhaarFile?.let { add(it.toFilePart("aadhaar_file")) }
            panFile?.let { add(it.toFilePart("pan_file")) }
        }

        private fun TruckOwnerBusinessEditParams.toParts() = buildList {
            businessName?.let { add(it.toPart("business_name")) }
            gstNumber?.let { add(it.toPart("gst_number")) }
            add(isTdsApplicable.toString().toPart("is_tds_applicable"))
            baseCityId?.let { add(it.toString().toPart("base_city_id")) }
            baseAreaId?.let { add(it.toString().toPart("base_area_id")) }
            add(
                buildJsonArray {
                    preferredRoutes.forEach { route ->
                        add(
                            buildJsonObject {
                                put("source_city_id", route.sourceCityId)
                                put("destination_city_id", route.destinationCityId)
                                put("priority", route.priority)
                            },
                        )
                    }
                }.toString().toPart("preferred_routes"),
            )
            gstFile?.let { add(it.toFilePart("gst_file")) }
            companyFile?.let { add(it.toFilePart("company_file")) }
            tdsFile?.let { add(it.toFilePart("tds_file")) }
        }

        private fun TruckOwnerBankEditParams.toParts() = buildList {
            add(accountHolderName.toPart("account_holder_name"))
            add(accountNumber.toPart("account_number"))
            add(ifscCode.toPart("ifsc_code"))
            upiId?.let { add(it.toPart("upi_id")) }
            bankName?.let { add(it.toPart("bank_name")) }
            branchName?.let { add(it.toPart("branch_name")) }
            chequeFile?.let { add(it.toFilePart("cheque_file")) }
        }

        private fun String.toPart(name: String): MultipartBody.Part = MultipartBody.Part.createFormData(name, this)

        private fun File.toFilePart(name: String): MultipartBody.Part {
            val mimeType = if (extension.equals("pdf", true)) "application/pdf" else "application/octet-stream"
            return MultipartBody.Part.createFormData(name, this.name, asRequestBody(mimeType.toMediaTypeOrNull()))
        }
    }
