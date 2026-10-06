package com.freight24.connect.repository

import com.freight24.network.error.ApiException
import com.freight24.rest.api.ClientV2Api
import com.freight24.rest.api.TruckOwnerV2Api
import com.freight24.rest.api.TruckV2Api
import com.freight24.rest.response.ClientOnboardingV2Data
import com.freight24.rest.response.ClientOnboardingV2Response
import com.freight24.rest.response.CreateTruckV2Dto
import com.freight24.rest.response.CreateTruckV2Response
import com.freight24.rest.response.TruckOwnerOnboardingV2Response
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
                api = unusedApi(),
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
                api = unusedApi(),
                v2Api = v2Api,
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
