package com.freight24.connect.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.database.dao.LeadsCacheDao
import com.freight24.database.entity.LeadCityEntity
import com.freight24.database.entity.LeadTruckTypeEntity
import com.freight24.rest.api.IndentsApi
import com.freight24.rest.api.LeadsApi
import com.freight24.rest.api.LocationApi
import com.freight24.rest.request.CallOutcomeRequest
import com.freight24.rest.request.ConvertLeadRequest
import com.freight24.rest.request.UpdateLeadDetailsRequest
import com.freight24.rest.response.CallOutcomeData
import com.freight24.rest.response.CityResult
import com.freight24.rest.response.ConvertLeadData
import com.freight24.rest.response.LeadDetailDto
import com.freight24.rest.response.LeadSummaryData
import com.freight24.rest.response.LeadTruckTypeDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class LeadsRepositoryImpl
    @Inject
    constructor(
        private val leadsApi: LeadsApi,
        private val locationApi: LocationApi,
        private val indentsApi: IndentsApi,
        private val leadsCacheDao: LeadsCacheDao,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : LeadsRepository {
        override suspend fun getLeads(
            status: String?,
            search: String?,
            page: Int,
            limit: Int,
        ): Result<LeadsPage> =
            withContext(ioDispatcher) {
                try {
                    val response = leadsApi.getLeads(status = status, search = search, page = page, limit = limit)
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        Result.success(
                            LeadsPage(
                                leads = body.data,
                                page = body.pagination.page,
                                totalPages = body.pagination.totalPages,
                                total = body.pagination.total,
                            ),
                        )
                    } else {
                        Result.failure(Exception(extractErrorMessage(response, body?.message)))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun getLeadSummary(): Result<LeadSummaryData> =
            withContext(ioDispatcher) {
                try {
                    val response = leadsApi.getLeadSummary()
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        Result.success(body.data)
                    } else {
                        Result.failure(Exception(extractErrorMessage(response, body?.message)))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun getLeadDetail(leadId: String): Result<LeadDetailDto> =
            withContext(ioDispatcher) {
                try {
                    val response = leadsApi.getLeadDetail(leadId)
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        Result.success(body.data)
                    } else {
                        Result.failure(Exception(extractErrorMessage(response, body?.message)))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun updateLeadDetails(
            leadId: String,
            request: UpdateLeadDetailsRequest,
        ): Result<LeadDetailDto> =
            withContext(ioDispatcher) {
                try {
                    val response = leadsApi.updateLeadDetails(leadId, request)
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        Result.success(body.data)
                    } else {
                        Result.failure(Exception(extractErrorMessage(response, body?.message)))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun recordCallOutcome(
            leadId: String,
            request: CallOutcomeRequest,
        ): Result<CallOutcomeData> =
            withContext(ioDispatcher) {
                try {
                    val response = leadsApi.recordCallOutcome(leadId, request)
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        Result.success(body.data)
                    } else {
                        Result.failure(Exception(extractErrorMessage(response, body?.message)))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun reopenLead(leadId: String): Result<Boolean> =
            withContext(ioDispatcher) {
                try {
                    val response = leadsApi.reopenLead(leadId)
                    val body = response.body()
                    if (response.isSuccessful && body != null && body.success) {
                        Result.success(true)
                    } else {
                        Result.failure(Exception(extractErrorMessage(response, body?.message)))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun convertLead(
            leadId: String,
            request: ConvertLeadRequest,
        ): Result<ConvertLeadData> =
            withContext(ioDispatcher) {
                try {
                    val response = leadsApi.convertLead(leadId, request)
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        Result.success(body.data)
                    } else {
                        Result.failure(Exception(extractErrorMessage(response, body?.message)))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun getTruckTypesCached(): Result<List<LeadTruckTypeDto>> =
            withContext(ioDispatcher) {
                val fresh =
                    runCatching { leadsCacheDao.getTruckTypes(System.currentTimeMillis() - CACHE_TTL_MS) }
                        .getOrDefault(emptyList())
                if (fresh.isNotEmpty()) {
                    return@withContext Result.success(fresh.map { it.toDto() })
                }

                val fetched = fetchTruckTypesFromMaster()
                if (fetched != null) return@withContext Result.success(fetched)

                // Offline fallback: serve stale cache entries
                val stale = runCatching { leadsCacheDao.getTruckTypes(0) }.getOrDefault(emptyList())
                if (stale.isNotEmpty()) {
                    Result.success(stale.map { it.toDto() })
                } else {
                    Result.failure(Exception("Could not load truck types"))
                }
            }

        override suspend fun searchCities(query: String): Result<List<CityResult>> =
            withContext(ioDispatcher) {
                try {
                    val response = locationApi.searchCities(query, "CITY")
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        val cities = body.data.filter { it.type == "CITY" }
                        runCatching {
                            leadsCacheDao.upsertCities(
                                cities.map {
                                    LeadCityEntity(
                                        id = it.id,
                                        name = it.name.ifBlank { it.city },
                                        cachedAt = System.currentTimeMillis(),
                                    )
                                },
                            )
                        }
                        Result.success(cities)
                    } else {
                        Result.failure(Exception(extractErrorMessage(response)))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun searchCachedCities(query: String): List<CityResult> =
            withContext(ioDispatcher) {
                runCatching { leadsCacheDao.searchCachedCities(query) }
                    .getOrDefault(emptyList())
                    // The cache stores only id + name, so the second line is reconstructed
                    // from the name and no parent city / state is claimed.
                    .map {
                        CityResult(
                            type = "CITY",
                            id = it.id,
                            name = it.name,
                            city = it.name,
                            state = "",
                        )
                    }
            }

        private suspend fun fetchTruckTypesFromMaster(): List<LeadTruckTypeDto>? =
            try {
                // IndentsApi.getTruckTypes() returns categories grouped open/closed/trailer
                val response = indentsApi.getTruckTypes()
                val body = response.body()
                if (response.isSuccessful && body != null && body.success) {
                    val category = body.data
                    val flat =
                        (category.open + category.closed + category.trailer).mapNotNull { dto ->
                            val id = dto.id.toIntOrNull() ?: return@mapNotNull null
                            LeadTruckTypeDto(id = id, truckType = dto.truckType, displayName = dto.displayName.ifBlank { dto.truckType })
                        }
                    runCatching {
                        leadsCacheDao.upsertTruckTypes(
                            flat.map { LeadTruckTypeEntity(it.id, it.truckType, it.displayName, System.currentTimeMillis()) },
                        )
                    }
                    flat
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }

        private fun LeadTruckTypeEntity.toDto() = LeadTruckTypeDto(id = id, truckType = truckType, displayName = displayName)

        private fun extractErrorMessage(
            response: Response<*>,
            bodyMessage: String? = null,
        ): String {
            bodyMessage?.takeIf { it.isNotBlank() }?.let { return it }

            val rawError = response.errorBody()?.string().orEmpty()
            if (rawError.isNotBlank()) {
                runCatching { JSONObject(rawError).optString("message") }
                    .getOrNull()
                    ?.takeIf { it.isNotBlank() }
                    ?.let { return it }
            }

            return response.message().ifBlank { "Request failed" }
        }

        companion object {
            private const val CACHE_TTL_MS = 24L * 60 * 60 * 1000
        }
    }
