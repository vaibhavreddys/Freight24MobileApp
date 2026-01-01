package com.freight24.trucker.feature.loads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.core.auth.AuthRepository
import com.freight24.core.common.model.CityLoadSummary
import com.freight24.core.common.model.CityPaginationState
import com.freight24.core.common.repository.LoadBoardRepository
import com.freight24.trucker.feature.loads.mapper.toUi
import com.freight24.trucker.feature.loads.model.LoadBoardItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LoadBoardViewModel @Inject constructor(
    private val repository: LoadBoardRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val pageSize = 10

    private val _expandedCityId = MutableStateFlow<Int?>(null)
    val expandedCityId: StateFlow<Int?> = _expandedCityId

    val cityData = mutableMapOf<Int, CityLoadSummary>()

    private val _items = MutableStateFlow<List<LoadBoardItem>>(emptyList())
    val items: StateFlow<List<LoadBoardItem>> = _items

    private val cityPagination =
        mutableMapOf<Int, CityPaginationState>()


    init {
        viewModelScope.launch {
            authRepository.authTokenFlow().filterNotNull().first()
            loadInitial()
        }
    }

    private suspend fun loadInitial() {
        repository.fetchLoadBoardSummary().forEach { summary ->
            cityData[summary.cityId] = summary
        }
        rebuild()
    }

    fun onCityClicked(cityId: Int) {
        if (_expandedCityId.value == cityId) {
            collapse()
        } else {
            expand(cityId)
        }
    }

    private fun expand(cityId: Int) {
        _expandedCityId.value = cityId

        cityPagination[cityId] = CityPaginationState()

        viewModelScope.launch {
            val result = repository.fetchLoadsForCity(cityId, page = 1)

            cityPagination[cityId] = CityPaginationState(
                page = 1,
                hasMore = result.hasMore
            )

            buildItems(
                cityId = cityId,
                loads = result.loads,
                hasMore = result.hasMore,
                total = result.totalLoads
            )
        }
    }

    fun onShowMoreClicked(cityId: Int) {
        val state = cityPagination[cityId] ?: return
        if (!state.hasMore || state.isLoading) return

        cityPagination[cityId] = state.copy(isLoading = true)

        viewModelScope.launch {
            val nextPage = state.page + 1
            val result = repository.fetchLoadsForCity(cityId, nextPage)

            cityPagination[cityId] = state.copy(
                page = nextPage,
                hasMore = result.hasMore,
                isLoading = false
            )

            appendLoads(
                cityId = cityId,
                loads = result.loads,
                hasMore = result.hasMore
            )
        }
    }

    private fun collapse() {
        _expandedCityId.value = null
        rebuild()
    }

    private fun buildItems(
        cityId: Int,
        loads: List<com.freight24.core.common.model.Load>,
        hasMore: Boolean,
        total: Int
    ) {
        val city = cityData[cityId] ?: return

        _items.value = buildList {
            cityData.values.forEach { c ->
                val expanded = c.cityId == cityId

                add(
                    LoadBoardItem.CityItem(
                        cityId = c.cityId,
                        cityName = c.cityName,
                        totalLoads = c.totalLoads,
                        expanded = expanded
                    )
                )

                if (expanded) {
                    addAll(loads.map { LoadBoardItem.LoadItem(it.toUi()) })

                    if (hasMore) {
                        add(
                            LoadBoardItem.ShowMoreItem(
                                cityId = cityId,
                                loadedCount = loads.size,
                                totalCount = total
                            )
                        )
                    }
                }
            }
        }
    }

    private fun appendLoads(
        cityId: Int,
        loads: List<com.freight24.core.common.model.Load>,
        hasMore: Boolean
    ) {
        _items.value = buildList {
            items.value.forEach { item ->
                when (item) {
                    is LoadBoardItem.ShowMoreItem -> {
                        if (item.cityId == cityId) {
                            // Insert new loads before Show More
                            addAll(loads.map { LoadBoardItem.LoadItem(it.toUi()) })

                            if (hasMore) {
                                add(item) // keep Show More
                            }
                        } else {
                            add(item)
                        }
                    }

                    else -> add(item)
                }
            }
        }
    }


    fun indexOfCity(cityId: Int): Int {
        return items.value.indexOfFirst {
            it is LoadBoardItem.CityItem && it.cityId == cityId
        }
    }


    private fun rebuild() {
        _items.value = buildList {
            cityData.values.forEach { city ->
                val expanded = _expandedCityId.value == city.cityId

                add(
                    LoadBoardItem.CityItem(
                        cityId = city.cityId,
                        cityName = city.cityName,
                        totalLoads = city.totalLoads,
                        expanded = expanded
                    )
                )

                if (expanded) {
                    addAll(
                        city.initialLoads.map {
                            LoadBoardItem.LoadItem(it.toUi())
                        }
                    )

                    // ShowMore handled ONLY via pagination state
                    val pagination = cityPagination[city.cityId]
                    if (pagination?.hasMore == true) {
                        add(
                            LoadBoardItem.ShowMoreItem(
                                cityId = city.cityId,
                                loadedCount = city.initialLoads.size,
                                totalCount = city.totalLoads
                            )
                        )
                    }
                }
            }
        }
    }
}