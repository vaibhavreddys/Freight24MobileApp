package com.freight24.network.repository

// import com.freight24.common.model.CityLoadSummary
// import com.freight24.common.model.PaginatedLoads
// import com.freight24.common.repository.LoadBoardRepository
// import com.freight24.network.api.LoadBoardApi
// import com.freight24.network.mapper.toDomain
// import kotlinx.coroutines.flow.Flow
// import kotlinx.coroutines.flow.flow
// import javax.inject.Inject
// import javax.inject.Singleton
//
// @Singleton
// class LoadBoardRepositoryImpl @Inject constructor(
//    private val api: LoadBoardApi
// ) : LoadBoardRepository {
//
//    override suspend fun fetchLoadBoardSummary(forceRefresh: Boolean): Flow<List<CityLoadSummary>> = flow {
//        val response = api.getLoadBoardSummary()
//        if (response.success) {
//            val summaries = response.data.orEmpty().map { dto ->
//                CityLoadSummary(
//                    cityId = dto.operationalCityId,
//                    cityName = dto.operationalCity,
//                    totalLoads = dto.totalLoads,
//                    initialLoads = dto.latestLoads.map { it.toDomain(dto.operationalCity) }
//                )
//            }
//            emit(summaries)
//        }
//    }
//
//    override suspend fun fetchLoadsForCity(
//        cityId: Int,
//        page: Int,
//        forceRefresh: Boolean
//    ): Flow<PaginatedLoads> = flow {
//
//        val response = api.getPaginatedLoads(cityId, page)
//        if (!response.success || response.data == null) {
//            emit(PaginatedLoads(emptyList(), page, false, 0))
//            return@flow
//        }
//
//        val fallbackCity =
//            response.data.loads.firstOrNull()?.loadingCity
//                ?: response.data.loads.firstOrNull()?.loadingArea
//                ?: "--"
//
//        val result = PaginatedLoads(
//            loads = response.data.loads.map { it.toDomain(fallbackCity) },
//            page = response.data.pagination.page,
//            hasMore = response.data.pagination.has_more,
//            totalLoads = response.data.pagination.total_loads
//        )
//        emit(result)
//    }
// }
