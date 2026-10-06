package com.freight24.connect.feature.clients.domain.usecases

import com.freight24.connect.repository.ClientRepository
import com.freight24.rest.response.ClientContactAssignmentRequest
import com.freight24.rest.response.ClientContactCreateRequest
import com.freight24.rest.response.ClientOnboardingBranchDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File
import javax.inject.Inject

data class ClientProfileV2Params(
    val clientType: String,
    val companyName: String,
    val displayName: String?,
    val companyType: String,
    val panCard: String,
    val gstNumber: String?,
    val email: String?,
    val phone: String?,
    val addressLine1: String,
    val cityId: Int,
    val stateId: Int,
    val pinCode: String,
    val panFile: File?,
    val gstFile: File?,
    val companyRegistrationFile: File?,
    val contractFile: File?,
)

data class ClientBranchV2Params(
    val name: String,
    val code: String?,
    val typeId: String,
    val addressLine1: String,
    val cityId: Int,
    val stateId: Int,
    val pinCode: String,
    val gstin: String?,
)

data class ClientLocationsV2Params(
    val operatingStructure: String,
    val headOfficeName: String,
    val branches: List<ClientBranchV2Params>,
)

data class ClientContactV2Params(
    val fullName: String,
    val phone: String,
    val email: String? = null,
    val designation: String? = null,
    val isPrimary: Boolean = false,
    val assignments: List<ClientContactScopeV2Params>,
)

data class ClientContactScopeV2Params(
    val branchId: String? = null,
    val roleIds: List<String>,
)

data class CreatedClientV2(val clientId: String, val userId: String)

interface CreateClientUseCase {
    suspend fun createProfile(params: ClientProfileV2Params): Result<CreatedClientV2>

    suspend fun saveLocations(clientId: String, params: ClientLocationsV2Params): Result<List<ClientOnboardingBranchDto>>

    suspend fun createContact(clientId: String, params: ClientContactV2Params): Result<Unit>
}

class CreateClientUseCaseImpl
    @Inject
    constructor(private val repository: ClientRepository) : CreateClientUseCase {
        override suspend fun createProfile(params: ClientProfileV2Params): Result<CreatedClientV2> {
            val parts = buildList {
                add(params.clientType.toPart("client_type"))
                add(params.companyName.toPart("company_name"))
                params.displayName?.let { add(it.toPart("display_name")) }
                add(params.companyType.toPart("company_type"))
                add(params.panCard.toPart("pan_card"))
                params.gstNumber?.let { add(it.toPart("gst_number")) }
                params.email?.let { add(it.toPart("company_email")) }
                params.phone?.let { add(it.toPart("company_phone")) }
                add(params.addressLine1.toPart("registered_address_line1"))
                add(params.cityId.toString().toPart("registered_city_id"))
                add(params.stateId.toString().toPart("registered_state_id"))
                add(params.pinCode.toPart("registered_pin_code"))
                add("true".toPart("billing_same_as_registered"))
                add("true".toPart("default_pod_same_as_registered"))
                params.panFile?.let { add(it.toFilePart("pan_file")) }
                params.gstFile?.let { add(it.toFilePart("gst_certificate_file")) }
                params.companyRegistrationFile?.let { add(it.toFilePart("company_registration_file")) }
                params.contractFile?.let { add(it.toFilePart("contract_file")) }
            }
            return repository.createClientV2(parts).mapCatching { response ->
                val data = response.data ?: error("Client creation returned no data")
                CreatedClientV2(data.clientId, data.userId ?: error("Client creation returned no user ID"))
            }
        }

        override suspend fun saveLocations(
            clientId: String,
            params: ClientLocationsV2Params,
        ): Result<List<ClientOnboardingBranchDto>> {
            val parts = buildList {
                add(params.operatingStructure.toPart("operating_structure"))
                if (params.operatingStructure == "SINGLE") {
                    add(params.headOfficeName.toPart("branch_name"))
                    add("HO001".toPart("branch_code"))
                } else if (params.operatingStructure == "MULTIPLE") {
                    val array = buildJsonArray {
                        params.branches.forEach { branch ->
                            add(buildJsonObject {
                                put("branch_name", branch.name)
                                branch.code?.let { put("branch_code", it) } ?: put("branch_code", JsonNull)
                                put("branch_type_id", branch.typeId)
                                put("address_line1", branch.addressLine1)
                                put("city_id", branch.cityId)
                                put("state_id", branch.stateId)
                                put("pin_code", branch.pinCode)
                                put("billing_same_as_branch", true)
                                put("pod_same_as_branch", true)
                                branch.gstin?.let { put("gstin", it) } ?: put("gstin", JsonNull)
                                branch.gstin?.let { put("gst_legal_name", params.headOfficeName) }
                                    ?: put("gst_legal_name", JsonNull)
                                put("gst_address_same_as_branch", true)
                            })
                        }
                    }
                    add(array.toString().toPart("branches"))
                }
            }
            return repository.saveClientLocations(clientId, parts).map { it.data?.branches.orEmpty() }
        }

        override suspend fun createContact(clientId: String, params: ClientContactV2Params): Result<Unit> {
            return repository.createClientContact(
                clientId,
                ClientContactCreateRequest(
                    fullName = params.fullName,
                    designation = params.designation,
                    phone = params.phone,
                    email = params.email,
                    isPrimary = params.isPrimary,
                    accessAssignments = params.assignments.map { assignment ->
                        ClientContactAssignmentRequest(
                            scopeType = if (assignment.branchId == null) "CLIENT" else "BRANCH",
                            branchId = assignment.branchId,
                            roleIds = assignment.roleIds.distinct(),
                        )
                    },
                ),
            )
        }

        private fun String.toPart(name: String): MultipartBody.Part = MultipartBody.Part.createFormData(name, this)

        private fun File.toFilePart(name: String): MultipartBody.Part {
            val mime = if (extension.equals("pdf", true)) "application/pdf" else "application/octet-stream"
            return MultipartBody.Part.createFormData(name, this.name, asRequestBody(mime.toMediaTypeOrNull()))
        }
    }
