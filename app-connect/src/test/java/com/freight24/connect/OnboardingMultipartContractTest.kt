package com.freight24.connect

import com.freight24.connect.feature.clients.domain.usecases.ClientLocationsV2Params
import com.freight24.connect.feature.clients.domain.usecases.ClientProfileV2Params
import com.freight24.connect.feature.clients.domain.usecases.ClientBranchV2Params
import com.freight24.connect.feature.clients.domain.usecases.ClientContactScopeV2Params
import com.freight24.connect.feature.clients.domain.usecases.ClientContactV2Params
import com.freight24.connect.feature.clients.domain.usecases.CreateClientUseCaseImpl
import com.freight24.connect.feature.truckowners.domain.usecases.CreateTruckOwnerUseCaseImpl
import com.freight24.connect.feature.truckowners.domain.usecases.TruckOwnerBankParams
import com.freight24.connect.feature.truckowners.domain.usecases.TruckOwnerBusinessParams
import com.freight24.connect.feature.truckowners.domain.usecases.TruckOwnerPersonalParams
import com.freight24.connect.feature.trucks.domain.usecases.AddTruckParams
import com.freight24.connect.feature.trucks.domain.usecases.AddTruckUseCaseImpl
import com.freight24.connect.repository.ClientRepository
import com.freight24.connect.repository.TruckOwnerRepository
import com.freight24.connect.repository.TruckRepository
import com.freight24.rest.request.UpdateClientRequest
import com.freight24.rest.response.*
import kotlinx.coroutines.runBlocking
import okhttp3.MultipartBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class OnboardingMultipartContractTest {
    @Test
    fun `truck creation sends required fields and omits absent optional fields`() = runBlocking {
        val repository = CapturingTruckRepository()
        val result = AddTruckUseCaseImpl(repository)(
            AddTruckParams(
                registrationNumber = "TN01AB1234",
                truckTypeId = 12,
                ownerProfileId = "owner-1",
                capacityMt = "16.5",
                rcCard = tempPdf("rc"),
            ),
        )

        assertTrue(result.isSuccess)
        assertEquals("truck-1", result.getOrThrow().truckId)
        assertEquals(
            setOf("registration_number", "truck_type_id", "truck_owner_id", "capacity_mt", "rc_card"),
            repository.parts.map(::partName).toSet(),
        )
        assertFalse(repository.parts.map(::partName).containsAll(listOf("make", "model", "year", "fc_certificate", "insurance")))
    }

    @Test
    fun `truck owner steps use returned id and exact multipart names`() = runBlocking {
        val repository = CapturingTruckOwnerRepository()
        val useCase = CreateTruckOwnerUseCaseImpl(repository)
        val userId = useCase.createPersonal(
            TruckOwnerPersonalParams(
                fullName = "ABC Transport",
                email = null,
                phone = "+919876543210",
                alternativePhone = null,
                panCard = "ABCDE1234F",
                aadhaarCard = "123412341234",
                aadhaarFile = tempPdf("aadhaar"),
                panFile = tempPdf("pan"),
            ),
        ).getOrThrow()

        useCase.saveBusiness(
            userId,
            TruckOwnerBusinessParams(null, null, true, null, emptyList(), null, null, null),
        ).getOrThrow()
        useCase.saveBank(
            userId,
            TruckOwnerBankParams("ABC Transport", "123456789", "HDFC0001234", null, null, null, tempPdf("cheque")),
        ).getOrThrow()

        assertEquals("owner-user-1", userId)
        assertEquals(
            setOf("full_name", "phone", "pan_card", "aadhar_card", "aadhaar_file", "pan_file"),
            repository.personalParts.map(::partName).toSet(),
        )
        assertEquals("owner-user-1", repository.businessUserId)
        assertEquals(setOf("is_tds_applicable", "preferred_routes"), repository.businessParts.map(::partName).toSet())
        assertEquals("owner-user-1", repository.bankUserId)
        assertEquals(
            setOf("account_holder_name", "account_number", "ifsc_code", "cheque_file"),
            repository.bankParts.map(::partName).toSet(),
        )
    }

    @Test
    fun `truck owner later-step failure is returned without another personal create`() = runBlocking {
        val repository = CapturingTruckOwnerRepository(failBusiness = true)
        val useCase = CreateTruckOwnerUseCaseImpl(repository)
        val userId = useCase.createPersonal(
            TruckOwnerPersonalParams(
                "ABC Transport", null, "+919876543210", null, "ABCDE1234F", "123412341234",
                tempPdf("aadhaar"), tempPdf("pan"),
            ),
        ).getOrThrow()

        val result = useCase.saveBusiness(
            userId,
            TruckOwnerBusinessParams(null, null, true, null, emptyList(), null, null, null),
        )

        assertTrue(result.isFailure)
        assertEquals(1, repository.personalCalls)
        assertEquals("owner-user-1", userId)
    }

    @Test
    fun `client profile and location contracts omit null optionals`() = runBlocking {
        val repository = CapturingClientRepository()
        val useCase = CreateClientUseCaseImpl(repository)
        val created = useCase.createProfile(
            ClientProfileV2Params(
                clientType = "DIRECT_CUSTOMER",
                companyName = "ABC Logistics",
                displayName = null,
                companyType = "PRIVATE_LIMITED",
                panCard = "ABCDE1234F",
                gstNumber = null,
                email = null,
                phone = "+919876543210",
                addressLine1 = "Chennai",
                cityId = 1,
                stateId = 2,
                pinCode = "600001",
                panFile = null,
                gstFile = null,
                companyRegistrationFile = null,
                contractFile = null,
            ),
        ).getOrThrow()
        val branches = useCase.saveLocations(
            created.clientId,
            ClientLocationsV2Params("SKIP", "", emptyList()),
        ).getOrThrow()

        assertEquals("client-1", created.clientId)
        assertEquals("client-user-1", created.userId)
        assertEquals(
            setOf(
                "client_type", "company_name", "company_type", "pan_card", "company_phone",
                "registered_address_line1", "registered_city_id", "registered_state_id", "registered_pin_code",
                "billing_same_as_registered", "default_pod_same_as_registered",
            ),
            repository.profileParts.map(::partName).toSet(),
        )
        assertEquals(setOf("operating_structure"), repository.locationParts.map(::partName).toSet())
        assertTrue(branches.isEmpty())
    }

    @Test
    fun `client location structures and contact scopes use v2 contracts`() = runBlocking {
        val repository = CapturingClientRepository()
        val useCase = CreateClientUseCaseImpl(repository)

        useCase.saveLocations("client-1", ClientLocationsV2Params("SINGLE", "Head Office", emptyList())).getOrThrow()
        useCase.saveLocations(
            "client-1",
            ClientLocationsV2Params(
                "MULTIPLE",
                "ABC Logistics",
                listOf(ClientBranchV2Params("Chennai", "CHE", "head-office", "Guindy", 1, 2, "600032", null)),
            ),
        ).getOrThrow()
        useCase.createContact(
            "client-1",
            ClientContactV2Params(
                fullName = "Ravi",
                phone = "+919876543210",
                assignments = listOf(
                    ClientContactScopeV2Params(null, listOf("operations", "operations")),
                    ClientContactScopeV2Params("branch-1", listOf("pod")),
                ),
            ),
        ).getOrThrow()

        assertEquals(setOf("operating_structure", "branch_name", "branch_code"), repository.locationCalls[0].map(::partName).toSet())
        assertEquals(setOf("operating_structure", "branches"), repository.locationCalls[1].map(::partName).toSet())
        assertEquals(listOf("operations"), repository.contactRequest?.accessAssignments?.first()?.roleIds)
        assertEquals("CLIENT", repository.contactRequest?.accessAssignments?.first()?.scopeType)
        assertEquals("BRANCH", repository.contactRequest?.accessAssignments?.last()?.scopeType)
    }

    private fun tempPdf(prefix: String): File = File.createTempFile(prefix.padEnd(3, '_'), ".pdf").apply {
        writeText("test")
        deleteOnExit()
    }

    private fun partName(part: MultipartBody.Part): String =
        Regex("name=\"([^\"]+)\"").find(part.headers?.get("Content-Disposition").orEmpty())!!.groupValues[1]
}

private class CapturingTruckRepository : TruckRepository {
    var parts = emptyList<MultipartBody.Part>()

    override suspend fun addTruck(
        registrationNumber: MultipartBody.Part,
        truckTypeId: MultipartBody.Part,
        ownerProfileId: MultipartBody.Part,
        make: MultipartBody.Part?,
        model: MultipartBody.Part?,
        year: MultipartBody.Part?,
        capacityMt: MultipartBody.Part?,
        rcCard: MultipartBody.Part?,
        fcCertificate: MultipartBody.Part?,
        insurance: MultipartBody.Part?,
    ): Result<CreateTruckV2Response> {
        parts = listOfNotNull(registrationNumber, truckTypeId, ownerProfileId, make, model, year, capacityMt, rcCard, fcCertificate, insurance)
        return Result.success(
            CreateTruckV2Response(
                true,
                "Created",
                CreateTruckV2Dto("truck-1", "TN01AB1234", "inactive", false, false, "AVAILABLE"),
            ),
        )
    }

    override suspend fun getTruckDocumentSignedUrl(truckId: String, documentType: String) = error("unused")
    override suspend fun updateTruck(truckId: String, registrationNumber: MultipartBody.Part, truckTypeId: MultipartBody.Part, make: MultipartBody.Part?, model: MultipartBody.Part?, year: MultipartBody.Part?, capacityMt: MultipartBody.Part?, rcCard: MultipartBody.Part?, fcCertificate: MultipartBody.Part?, insurance: MultipartBody.Part?) = error("unused")
    override suspend fun getTruckManagementPage(query: String?, status: String?, documentStatus: String?, page: Int, limit: Int) = error("unused")
    override suspend fun searchAssignmentTrucks(query: String, page: Int, limit: Int) = error("unused")
    override suspend fun getTruckDetails(truckId: String) = error("unused")
    override suspend fun validateRegistration(registrationNumber: String, excludeTruckId: String?) = error("unused")
}

private class CapturingTruckOwnerRepository(private val failBusiness: Boolean = false) : TruckOwnerRepository {
    var personalCalls = 0
    var personalParts = emptyList<MultipartBody.Part>()
    var businessParts = emptyList<MultipartBody.Part>()
    var bankParts = emptyList<MultipartBody.Part>()
    var businessUserId: String? = null
    var bankUserId: String? = null

    override suspend fun createPersonalProfile(parts: List<MultipartBody.Part>): Result<TruckOwnerOnboardingV2Response> {
        personalCalls++
        personalParts = parts
        return Result.success(TruckOwnerOnboardingV2Response(true, "Created", data = TruckOwnerOnboardingV2Data("owner-user-1")))
    }

    override suspend fun updateBusinessProfile(ownerUserId: String, parts: List<MultipartBody.Part>): Result<TruckOwnerOnboardingV2Response> {
        businessUserId = ownerUserId
        businessParts = parts
        return if (failBusiness) Result.failure(IllegalStateException("Business failed")) else Result.success(TruckOwnerOnboardingV2Response(true, "Saved"))
    }

    override suspend fun updateBankProfile(ownerUserId: String, parts: List<MultipartBody.Part>): Result<TruckOwnerOnboardingV2Response> {
        bankUserId = ownerUserId
        bankParts = parts
        return Result.success(TruckOwnerOnboardingV2Response(true, "Saved"))
    }

    override suspend fun getTruckOwners() = error("unused")
    override suspend fun searchTruckOwners(query: String) = error("unused")
}

private class CapturingClientRepository : ClientRepository {
    var profileParts = emptyList<MultipartBody.Part>()
    var locationParts = emptyList<MultipartBody.Part>()
    val locationCalls = mutableListOf<List<MultipartBody.Part>>()
    var contactRequest: ClientContactCreateRequest? = null

    override suspend fun createClientV2(parts: List<MultipartBody.Part>): Result<ClientOnboardingV2Response> {
        profileParts = parts
        return Result.success(ClientOnboardingV2Response(true, data = ClientOnboardingV2Data("client-1", "client-user-1")))
    }

    override suspend fun saveClientLocations(clientId: String, parts: List<MultipartBody.Part>): Result<ClientOnboardingV2Response> {
        locationParts = parts
        locationCalls += parts
        return Result.success(ClientOnboardingV2Response(true, data = ClientOnboardingV2Data(clientId, operatingStructure = "SKIP")))
    }

    override suspend fun createClientContact(clientId: String, request: ClientContactCreateRequest): Result<Unit> {
        contactRequest = request
        return Result.success(Unit)
    }

    override suspend fun getClients() = error("unused")
    override suspend fun searchClients(query: String) = error("unused")
    override suspend fun getClientByUserId(clientUserId: String) = error("unused")
    override suspend fun createClient(request: UpdateClientRequest) = error("unused")
    override suspend fun getStates() = error("unused")
    override suspend fun getCities(stateId: Int) = error("unused")
    override suspend fun getBranchTypes() = error("unused")
    override suspend fun getContactRoles() = error("unused")
    override suspend fun updateClient(clientUserId: String, request: UpdateClientRequest) = error("unused")
    override suspend fun getClientBranches(clientId: String) = error("unused")
    override suspend fun getClientContacts(clientId: String) = error("unused")
}
