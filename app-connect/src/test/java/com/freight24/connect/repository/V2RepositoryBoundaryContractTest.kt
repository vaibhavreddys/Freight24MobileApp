package com.freight24.connect.repository

import com.freight24.network.error.ApiException
import com.freight24.rest.api.ClientV2Api
import com.freight24.rest.api.TruckOwnerV2Api
import com.freight24.rest.api.TruckV2Api
import com.freight24.rest.response.ClientOnboardingV2Data
import com.freight24.rest.response.ClientOnboardingV2Response
import com.freight24.rest.response.ClientV2CompanyDto
import com.freight24.rest.response.ClientV2Counts
import com.freight24.rest.response.ClientV2DetailDto
import com.freight24.rest.response.ClientV2DetailResponse
import com.freight24.rest.response.ClientV2PageResponse
import com.freight24.rest.response.ClientV2SummaryDto
import com.freight24.rest.response.ClientV2FieldValidationDto
import com.freight24.rest.response.ClientV2ValidationData
import com.freight24.rest.response.ClientV2ValidationResponse
import com.freight24.rest.response.SignedDocument
import com.freight24.rest.api.DocumentApi
import com.freight24.rest.response.CreateTruckV2Dto
import com.freight24.rest.response.CreateTruckV2Response
import com.freight24.rest.response.TruckOwnerOnboardingV2Response
import com.freight24.rest.response.TruckOwnerV2Counts
import com.freight24.rest.response.TruckOwnerV2DetailDto
import com.freight24.rest.response.TruckOwnerV2DetailResponse
import com.freight24.rest.response.TruckOwnerV2FieldValidationDto
import com.freight24.rest.response.TruckOwnerV2PageResponse
import com.freight24.rest.response.TruckOwnerV2SummaryDto
import com.freight24.rest.response.TruckOwnerV2ValidationData
import com.freight24.rest.response.TruckOwnerV2ValidationResponse
import com.freight24.rest.response.TruckV2Counts
import com.freight24.rest.response.TruckV2DetailDto
import com.freight24.rest.response.TruckV2DetailResponse
import com.freight24.rest.response.TruckV2PageResponse
import com.freight24.rest.response.TruckV2SummaryDto
import com.freight24.rest.response.TruckV2ValidationData
import com.freight24.rest.response.TruckV2ValidationResponse
import com.freight24.rest.response.TruckV2ValidationTruckDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.lang.reflect.Proxy

class V2RepositoryBoundaryContractTest {
    @Test
    fun `management search forwards paging and maps server page`() = runBlocking {
        val v2Api =
            apiProxy<TruckV2Api> { method, arguments ->
                assertEquals("searchTrucks", method)
                assertEquals("TN01", arguments[0])
                assertEquals(2, arguments[3])
                assertEquals(30, arguments[4])
                Response.success(
                    TruckV2PageResponse(
                        success = true,
                        page = 2,
                        limit = 30,
                        counts = TruckV2Counts(total = 31),
                        data = listOf(TruckV2SummaryDto("truck-31", "TN01AB1234", "active")),
                    ),
                )
            }
        val repository = TruckRepositoryImpl(v2Api, unusedApi(), Dispatchers.Unconfined)

        val page = repository.getTruckManagementPage(query = "TN01", page = 2).getOrThrow()

        assertEquals("truck-31", page.items.single().truckId)
        assertTrue(page.endOfList)
    }

    @Test
    fun `detail preserves truck type id for editing`() = runBlocking {
        val v2Api =
            apiProxy<TruckV2Api> { method, arguments ->
                assertEquals("getTruckDetail", method)
                assertEquals("truck-1", arguments[0])
                Response.success(
                    TruckV2DetailResponse(
                        success = true,
                        data = TruckV2DetailDto("truck-1", 12, "TN01AB1234", "active"),
                    ),
                )
            }
        val repository = TruckRepositoryImpl(v2Api, unusedApi(), Dispatchers.Unconfined)

        val truck = repository.getTruckDetails("truck-1").getOrThrow()

        assertEquals(12, truck.truckTypeId)
    }

    @Test
    fun `registration validation forwards edit exclusion`() = runBlocking {
        val v2Api =
            apiProxy<TruckV2Api> { method, arguments ->
                assertEquals("validateRegistration", method)
                assertEquals("TN01AB1234", arguments[0])
                assertEquals("truck-1", arguments[1])
                Response.success(
                    TruckV2ValidationResponse(
                        success = true,
                        data =
                            TruckV2ValidationData(
                                exists = true,
                                truck = TruckV2ValidationTruckDto("truck-2", "TN01AB1234"),
                            ),
                    ),
                )
            }
        val repository = TruckRepositoryImpl(v2Api, unusedApi(), Dispatchers.Unconfined)

        val validation = repository.validateRegistration("TN01AB1234", "truck-1").getOrThrow()

        assertTrue(validation.exists)
        assertEquals("truck-2", validation.truckId)
    }

    @Test
    fun `truck update forwards multipart fields and maps enriched detail`() = runBlocking {
        val registrationPart = part("registration_number", "TN01AB1234")
        val v2Api =
            apiProxy<TruckV2Api> { method, arguments ->
                assertEquals("updateTruck", method)
                assertEquals("truck-1", arguments[0])
                assertSame(registrationPart, arguments[1])
                Response.success(
                    TruckV2DetailResponse(
                        success = true,
                        message = "Truck updated successfully",
                        data = TruckV2DetailDto("truck-1", 12, "TN01AB1234", "active"),
                    ),
                )
            }
        val repository = TruckRepositoryImpl(v2Api, unusedApi(), Dispatchers.Unconfined)

        val truck =
            repository.updateTruck(
                truckId = "truck-1",
                registrationNumber = registrationPart,
                truckTypeId = part("truck_type_id", "12"),
                make = null,
                model = null,
                year = null,
                capacityMt = part("capacity_mt", "16.5"),
            ).getOrThrow()

        assertEquals("truck-1", truck.truckId)
        assertEquals(12, truck.truckTypeId)
    }

    @Test
    fun `truck creation forwards owner profile id and returns truck id`() = runBlocking {
        val ownerProfilePart = part("truck_owner_id", "owner-profile-1")
        var forwardedOwnerProfilePart: MultipartBody.Part? = null
        val v2Api =
            apiProxy<TruckV2Api> { method, arguments ->
                assertEquals("createTruck", method)
                forwardedOwnerProfilePart = arguments[2] as MultipartBody.Part
                Response.success(
                    CreateTruckV2Response(
                        success = true,
                        message = "Created",
                        data =
                            CreateTruckV2Dto(
                                truckId = "truck-1",
                                registrationNumber = "TN01AB1234",
                                status = "inactive",
                                isVerified = false,
                                isBlacklisted = false,
                                availability = "AVAILABLE",
                            ),
                    ),
                )
            }
        val repository =
            TruckRepositoryImpl(
                v2Api = v2Api,
                documentApi = unusedApi(),
                ioDispatcher = Dispatchers.Unconfined,
            )

        val result =
            repository.addTruck(
                registrationNumber = part("registration_number", "TN01AB1234"),
                truckTypeId = part("truck_type_id", "12"),
                ownerProfileId = ownerProfilePart,
                make = null,
                model = null,
                year = null,
                capacityMt = part("capacity_mt", "16.5"),
                rcCard = null,
                fcCertificate = null,
                insurance = null,
            )

        assertTrue(result.isSuccess)
        assertEquals("truck-1", result.getOrThrow().data?.truckId)
        assertSame(ownerProfilePart, forwardedOwnerProfilePart)
    }

    @Test
    fun `truck creation preserves structured backend activation error`() = runBlocking {
        val v2Api =
            apiProxy<TruckV2Api> { _, _ ->
                Response.error<CreateTruckV2Response>(
                    422,
                    """{"success":false,"code":"ACTIVATION_REQUIREMENTS_MISSING","message":"Truck cannot be activated","details":{"missingRequirements":["registrationCertificate"]}}"""
                        .toResponseBody("application/json".toMediaType()),
                )
            }
        val repository =
            TruckRepositoryImpl(
                v2Api = v2Api,
                documentApi = unusedApi(),
                ioDispatcher = Dispatchers.Unconfined,
            )

        val result =
            repository.addTruck(
                registrationNumber = part("registration_number", "TN01AB1234"),
                truckTypeId = part("truck_type_id", "12"),
                ownerProfileId = part("truck_owner_id", "owner-profile-1"),
                make = null,
                model = null,
                year = null,
                capacityMt = part("capacity_mt", "16.5"),
                rcCard = null,
                fcCertificate = null,
                insurance = null,
            )

        val error = result.exceptionOrNull() as ApiException
        assertEquals(422, error.statusCode)
        assertEquals("ACTIVATION_REQUIREMENTS_MISSING", error.errorCode)
        assertEquals("Truck cannot be activated", error.message)
        assertTrue(error.details.toString().contains("registrationCertificate"))
    }

    @Test
    fun `truck owner business update forwards owner user id`() = runBlocking {
        var forwardedOwnerUserId: String? = null
        val v2Api =
            apiProxy<TruckOwnerV2Api> { method, arguments ->
                assertEquals("updateBusinessProfile", method)
                forwardedOwnerUserId = arguments[0] as String
                Response.success(TruckOwnerOnboardingV2Response(success = true, message = "Saved"))
            }
        val repository =
            TruckOwnerRepositoryImpl(
                v2Api = v2Api,
                ioDispatcher = Dispatchers.Unconfined,
            )
        val parts =
            listOf(
                part("is_tds_applicable", "true"),
                part("preferred_routes", "[]"),
            )

        val result = repository.updateBusinessProfile("owner-user-1", parts)

        assertTrue(result.isSuccess)
        assertEquals("owner-user-1", forwardedOwnerUserId)
    }

    @Test
    fun `truck owner management page forwards filters and maps explicit ids`() = runBlocking {
        val v2Api =
            apiProxy<TruckOwnerV2Api> { method, arguments ->
                assertEquals("getTruckOwners", method)
                assertEquals("active", arguments[0])
                assertEquals("pending_docs", arguments[2])
                assertEquals(2, arguments[3])
                Response.success(
                    TruckOwnerV2PageResponse(
                        success = true,
                        page = 2,
                        limit = 30,
                        counts = TruckOwnerV2Counts(total = 31),
                        data =
                            listOf(
                                TruckOwnerV2SummaryDto(
                                    id = "owner-user-31",
                                    userId = "owner-user-31",
                                    profileId = "owner-profile-31",
                                    fullName = "ABC Transport",
                                ),
                            ),
                    ),
                )
            }
        val repository = TruckOwnerRepositoryImpl(v2Api, Dispatchers.Unconfined)

        val page =
            repository.getManagementPage(
                status = "active",
                documentStatus = "pending_docs",
                page = 2,
            ).getOrThrow()

        assertEquals("owner-user-31", page.items.single().ownerUserId)
        assertEquals("owner-profile-31", page.items.single().ownerProfileId)
        assertTrue(page.endOfList)
    }

    @Test
    fun `truck owner picker uses basic search and profile id`() = runBlocking {
        val v2Api =
            apiProxy<TruckOwnerV2Api> { method, arguments ->
                assertEquals("searchTruckOwnerOptions", method)
                assertEquals("ABC", arguments[0])
                Response.success(
                    TruckOwnerV2PageResponse(
                        success = true,
                        page = 1,
                        limit = 30,
                        count = 1,
                        data =
                            listOf(
                                TruckOwnerV2SummaryDto(
                                    id = "owner-profile-1",
                                    userId = "owner-user-1",
                                    profileId = "owner-profile-1",
                                    fullName = "ABC Transport",
                                ),
                            ),
                    ),
                )
            }
        val repository = TruckOwnerRepositoryImpl(v2Api, Dispatchers.Unconfined)

        val owner = repository.searchPickerOptions("ABC").getOrThrow().items.single()

        assertEquals("owner-user-1", owner.ownerUserId)
        assertEquals("owner-profile-1", owner.ownerProfileId)
    }

    @Test
    fun `truck owner validation forwards user id exclusion`() = runBlocking {
        val v2Api =
            apiProxy<TruckOwnerV2Api> { method, arguments ->
                assertEquals("validateTruckOwner", method)
                assertEquals("ABCDE1234F", arguments[1])
                assertEquals("owner-user-1", arguments[4])
                Response.success(
                    TruckOwnerV2ValidationResponse(
                        success = true,
                        data =
                            TruckOwnerV2ValidationData(
                                pan = TruckOwnerV2FieldValidationDto(exists = false),
                            ),
                    ),
                )
            }
        val repository = TruckOwnerRepositoryImpl(v2Api, Dispatchers.Unconfined)

        val result = repository.validate(pan = "ABCDE1234F", excludeOwnerUserId = "owner-user-1").getOrThrow()

        assertEquals(false, result.pan?.exists)
    }

    @Test
    fun `truck owner status update maps returned detail`() = runBlocking {
        val v2Api =
            apiProxy<TruckOwnerV2Api> { method, arguments ->
                assertEquals("updateTruckOwnerStatus", method)
                assertEquals("owner-user-1", arguments[0])
                Response.success(
                    TruckOwnerV2DetailResponse(
                        success = true,
                        data =
                            TruckOwnerV2DetailDto(
                                id = "owner-user-1",
                                profileId = "owner-profile-1",
                                fullName = "ABC Transport",
                                status = "active",
                            ),
                    ),
                )
            }
        val repository = TruckOwnerRepositoryImpl(v2Api, Dispatchers.Unconfined)

        val owner = repository.updateStatus("owner-user-1", "active").getOrThrow()

        assertEquals("active", owner.status)
        assertEquals("owner-profile-1", owner.ownerProfileId)
    }

    @Test
    fun `client location update forwards client profile id`() = runBlocking {
        var forwardedClientId: String? = null
        val v2Api =
            apiProxy<ClientV2Api> { method, arguments ->
                assertEquals("saveLocations", method)
                forwardedClientId = arguments[0] as String
                Response.success(
                    ClientOnboardingV2Response(
                        success = true,
                        data = ClientOnboardingV2Data(clientId = "client-profile-1", userId = "client-user-1"),
                    ),
                )
            }
        val repository =
            ClientRepositoryImpl(
                v2Api = v2Api,
                documentApi = unusedApi(),
                referenceDataApi = unusedApi(),
                ioDispatcher = Dispatchers.Unconfined,
            )

        val result =
            repository.saveClientLocations(
                clientId = "client-profile-1",
                parts = listOf(part("registered_address", "{}")),
            )

        assertTrue(result.isSuccess)
        assertEquals("client-profile-1", result.getOrThrow().data?.clientId)
        assertEquals("client-profile-1", forwardedClientId)
    }

    @Test
    fun `client duplicate validation uses the v2 validation contract`() = runBlocking {
        val v2Api =
            apiProxy<ClientV2Api> { method, arguments ->
                assertEquals("validateClient", method)
                assertEquals("ABCDE1234F", arguments[0])
                assertEquals(null, arguments[1])
                Response.success(
                    ClientV2ValidationResponse(
                        success = true,
                        data =
                            ClientV2ValidationData(
                                pan_card = ClientV2FieldValidationDto(exists = true),
                            ),
                    ),
                )
            }
        val repository = ClientRepositoryImpl(v2Api, unusedApi(), unusedApi(), Dispatchers.Unconfined)

        val exists = repository.isClientFieldRegistered("pan_card", "ABCDE1234F").getOrThrow()

        assertTrue(exists)
    }

    @Test
    fun `client management page forwards server search and profile identifiers`() = runBlocking {
        val v2Api =
            apiProxy<ClientV2Api> { method, arguments ->
                assertEquals("getClients", method)
                assertEquals("active", arguments[0])
                assertEquals("Acme", arguments[1])
                assertEquals(2, arguments[2])
                Response.success(
                    ClientV2PageResponse(
                        success = true,
                        page = 2,
                        limit = 30,
                        counts = ClientV2Counts(total = 31, active = 31),
                        data = listOf(ClientV2SummaryDto("client-profile-31", "client-user-31", companyName = "Acme")),
                    ),
                )
            }
        val repository = ClientRepositoryImpl(v2Api, unusedApi(), unusedApi(), Dispatchers.Unconfined)

        val page = repository.getManagementPage("Acme", "active", page = 2).getOrThrow()

        assertEquals("client-profile-31", page.items.single().clientId)
        assertEquals("client-user-31", page.items.single().clientUserId)
        assertTrue(page.endOfList)
    }

    @Test
    fun `client picker and detail retain client profile id namespace`() = runBlocking {
        val v2Api =
            apiProxy<ClientV2Api> { method, arguments ->
                when (method) {
                    "getClients" ->
                        Response.success(
                            ClientV2PageResponse(
                                success = true,
                                page = 1,
                                limit = 30,
                                counts = ClientV2Counts(total = 1, active = 1),
                                data = listOf(ClientV2SummaryDto("client-profile-1", "client-user-1", companyName = "Acme")),
                            ),
                        )
                    "getClientDetail" -> {
                        assertEquals("client-profile-1", arguments[0])
                        Response.success(
                            ClientV2DetailResponse(
                                success = true,
                                data = ClientV2DetailDto(
                                    id = "client-user-1",
                                    clientId = "client-profile-1",
                                    company = ClientV2CompanyDto("Acme"),
                                ),
                            ),
                        )
                    }
                    else -> error("Unexpected ClientV2Api call: $method")
                }
            }
        val repository = ClientRepositoryImpl(v2Api, unusedApi(), unusedApi(), Dispatchers.Unconfined)

        val picker = repository.searchPickerOptions("Acme").getOrThrow().items.single()
        val detail = repository.getDetails(picker.clientId).getOrThrow()

        assertEquals("client-profile-1", picker.clientId)
        assertEquals("client-profile-1", detail.clientId)
        assertEquals("client-user-1", detail.clientUserId)
    }

    @Test
    fun `client document preview uses resource scoped client and branch routes`() = runBlocking {
        val documentApi =
            apiProxy<DocumentApi> { method, arguments ->
                when (method) {
                    "getClientDocumentSignedUrl" -> {
                        assertEquals(listOf("client-profile-1", "client-doc-1"), arguments.take(2))
                        Response.success(SignedDocument(SignedDocument.Document("client-doc-1", "https://signed/client", 600)))
                    }
                    "getClientBranchDocumentSignedUrl" -> {
                        assertEquals(listOf("client-profile-1", "branch-1", "branch-doc-1"), arguments.take(3))
                        Response.success(SignedDocument(SignedDocument.Document("branch-doc-1", "https://signed/branch", 600)))
                    }
                    else -> error("Unexpected DocumentApi call: $method")
                }
            }
        val repository = ClientRepositoryImpl(unusedApi(), documentApi, unusedApi(), Dispatchers.Unconfined)

        assertEquals("https://signed/client", repository.getClientDocumentSignedUrl("client-profile-1", "client-doc-1").getOrThrow())
        assertEquals("https://signed/branch", repository.getBranchDocumentSignedUrl("client-profile-1", "branch-1", "branch-doc-1").getOrThrow())
    }

    private fun part(name: String, value: String): MultipartBody.Part =
        MultipartBody.Part.createFormData(name, value)

    private inline fun <reified T> unusedApi(): T =
        apiProxy { method, _ -> error("Unexpected ${T::class.java.simpleName} call: $method") }

    private inline fun <reified T> apiProxy(
        crossinline invocation: (method: String, arguments: Array<out Any?>) -> Any?,
    ): T =
        Proxy.newProxyInstance(
            T::class.java.classLoader,
            arrayOf(T::class.java),
        ) { _, method, arguments ->
            invocation(method.name, arguments.orEmpty())
        } as T
}
