package com.freight24.trucker.feature.loads

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.common.usecase.EmptyUseCaseParams
import com.freight24.trucker.feature.loads.domain.usecase.CityLoadBoardUseCase
import com.freight24.trucker.feature.loads.domain.usecase.CityLoadBoardUseCaseParams
import com.freight24.trucker.feature.loads.domain.usecase.ExpressInterestUseCase
import com.freight24.trucker.feature.loads.domain.usecase.ExpressInterestUseCaseParams
import com.freight24.trucker.feature.loads.domain.usecase.LoadBoardUseCase
import com.freight24.trucker.feature.loads.mapper.toUi
import com.freight24.trucker.feature.loads.model.LoadBoardItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoadBoardViewModel
    @Inject
    constructor(
        private val loadBoardUseCase: LoadBoardUseCase,
        private val cityLoadBoardUseCase: CityLoadBoardUseCase,
        private val expressInterestUseCase: ExpressInterestUseCase,
    ) : ViewModel() {
        private val _items = MutableStateFlow<List<LoadBoardItem>>(emptyList())
        val items: StateFlow<List<LoadBoardItem>> = _items.asStateFlow()

        private val _expandedCityId = MutableStateFlow<Int?>(null)
        val expandedCityId: StateFlow<Int?> = _expandedCityId.asStateFlow()

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

        private val interestedLoadIds = mutableSetOf<String>()
        private val loadingInterestedLoadIds = mutableSetOf<String>()

        private val _bookmarkMessage = MutableSharedFlow<String>()
        val bookmarkMessage: SharedFlow<String> = _bookmarkMessage.asSharedFlow()

        // Track current page for each city
        private val cityPages = mutableMapOf<Int, Int>()

        init {
            viewModelScope.launch {
                loadBoardUseCase.invoke(EmptyUseCaseParams)
                    .onSuccess { summaryList ->
                        updateCityList(summaryList)
                    }
            }
        }

        /**
         * Handle pull-to-refresh action.
         * Forces a network fetch and updates the UI.
         */
        fun onRefresh() {
            viewModelScope.launch {
                _isRefreshing.value = true

                loadBoardUseCase
                    .invoke(EmptyUseCaseParams)
                    .onSuccess { summaryList ->
                        updateCityList(summaryList)

                        // 2. Refresh currently expanded city if any
                        _expandedCityId.value?.let { cityId ->
                            fetchLoadsForCity(cityId, page = 1, forceRefresh = true)
                        }
                        _isRefreshing.value = false
                    }.onFailure {
                        Log.e("LoadBoardViewModel", "Refresh failed", it)
                        _isRefreshing.value = false
                    }
            }
        }

        private fun updateCityList(summaryList: List<com.freight24.common.model.CityLoadSummary>) {
            val newItems = mutableListOf<LoadBoardItem>()
            summaryList.forEach { summary ->
                val isExpanded = _expandedCityId.value == summary.cityId
                newItems.add(
                    LoadBoardItem.CityItem(
                        cityId = summary.cityId,
                        cityName = summary.cityName,
                        totalLoads = summary.totalLoads,
                        expanded = isExpanded,
                    ),
                )

                // If this city is expanded, populate it with initial loads immediately
                if (isExpanded) {
                    summary.initialLoads.forEach { load ->
                        newItems.add(LoadBoardItem.LoadItem(load.toUi().decorateInterestState()))
                    }

                    // Add Show More if there are more loads
                    if (summary.totalLoads > summary.initialLoads.size) {
                        newItems.add(
                            LoadBoardItem.ShowMoreItem(
                                cityId = summary.cityId,
                                loadedCount = summary.initialLoads.size,
                                totalCount = summary.totalLoads,
                            ),
                        )
                    }
                }
            }
            _items.value = newItems
        }

        fun onCityClicked(cityId: String) {
            val cityIdInt = cityId.toIntOrNull() ?: return
            onCityClicked(cityIdInt)
        }

        fun onCityClicked(cityId: Int) {
            val currentItems = _items.value.toMutableList()
            val index =
                currentItems.indexOfFirst {
                    it is LoadBoardItem.CityItem && it.cityId == cityId
                }

            if (index == -1) return

            val cityItem = currentItems[index] as LoadBoardItem.CityItem
            val isExpanding = !cityItem.expanded

            // 1. Toggle the clicked city state
            currentItems[index] = cityItem.copy(expanded = isExpanding)

            // 2. Collapse any OTHER expanded city
            _expandedCityId.value?.let { otherId ->
                if (otherId != cityId) {
                    val otherIndex =
                        currentItems.indexOfFirst {
                            it is LoadBoardItem.CityItem && it.cityId == otherId
                        }
                    if (otherIndex != -1) {
                        val otherCity = currentItems[otherIndex] as LoadBoardItem.CityItem
                        // Remove its children
                        val nextIndex = otherIndex + 1
                        while (nextIndex < currentItems.size &&
                            (
                                currentItems[nextIndex] is LoadBoardItem.LoadItem ||
                                    currentItems[nextIndex] is LoadBoardItem.ShowMoreItem
                            )
                        ) {
                            currentItems.removeAt(nextIndex)
                        }
                        // Mark as collapsed
                        currentItems[otherIndex] = otherCity.copy(expanded = false)
                    }
                }
            }

            // 3. Update Expanded State
            if (isExpanding) {
                _expandedCityId.value = cityId
                cityPages[cityId] = 1
                fetchLoadsForCity(cityId, 1)
            } else {
                _expandedCityId.value = null
                cityPages.remove(cityId)
                // Remove children of THIS city
                val nextIndex = index + 1
                while (nextIndex < currentItems.size &&
                    (
                        currentItems[nextIndex] is LoadBoardItem.LoadItem ||
                            currentItems[nextIndex] is LoadBoardItem.ShowMoreItem
                    )
                ) {
                    currentItems.removeAt(nextIndex)
                }
            }

            _items.value = currentItems
        }

        private fun fetchLoadsForCity(
            cityId: Int,
            page: Int,
            forceRefresh: Boolean = false,
        ) {
            viewModelScope.launch {
                val params =
                    CityLoadBoardUseCaseParams(
                        cityId,
                        page,
                        forceRefresh,
                    )

                cityLoadBoardUseCase
                    .invoke(params)
                    .onSuccess { paginatedLoads ->
                        val currentItems = _items.value.toMutableList()
                        val cityIndex =
                            currentItems.indexOfFirst {
                                it is LoadBoardItem.CityItem && it.cityId == cityId
                            }

                        if (cityIndex == -1) return@launch

                        // Handling Page 1 vs Next Pages
                        var insertIndex: Int

                        if (page == 1) {
                            // --- PAGE 1: REPLACE ALL CHILDREN ---
                            // Find range of existing children and remove them
                            var nextIndex = cityIndex + 1
                            while (nextIndex < currentItems.size &&
                                (
                                    currentItems[nextIndex] is LoadBoardItem.LoadItem ||
                                        currentItems[nextIndex] is LoadBoardItem.ShowMoreItem
                                )
                            ) {
                                currentItems.removeAt(nextIndex)
                            }
                            insertIndex = cityIndex + 1
                        } else {
                            // --- PAGE > 1: APPEND ---
                            // Find the "Show More" item (should be the last child) and remove it
                            // The new items will be inserted where "Show More" was.

                            var lastChildIndex = cityIndex
                            var nextIndex = cityIndex + 1
                            while (nextIndex < currentItems.size &&
                                (
                                    currentItems[nextIndex] is LoadBoardItem.LoadItem ||
                                        currentItems[nextIndex] is LoadBoardItem.ShowMoreItem
                                )
                            ) {
                                lastChildIndex = nextIndex
                                nextIndex++
                            }

                            // If the last child is ShowMore, remove it and insert there
                            if (currentItems.getOrNull(lastChildIndex) is LoadBoardItem.ShowMoreItem) {
                                currentItems.removeAt(lastChildIndex)
                                insertIndex = lastChildIndex
                            } else {
                                // Just append after the last child
                                insertIndex = lastChildIndex + 1
                            }
                        }

                        // Insert New Children
                        paginatedLoads.loads.forEach { load ->
                            currentItems.add(insertIndex++, LoadBoardItem.LoadItem(load.toUi().decorateInterestState()))
                        }

                        // Add "Show More" if needed
                        if (paginatedLoads.hasMore) {
                            // Calculate loaded count approx
                            val loadedSoFar = cityPages[cityId]?.times(10) ?: 10 // Approximation or track exactly if needed

                            currentItems.add(
                                insertIndex,
                                LoadBoardItem.ShowMoreItem(
                                    cityId = cityId,
                                    loadedCount = loadedSoFar, // Placeholder or exact logic
                                    totalCount = paginatedLoads.totalLoads,
                                ),
                            )
                        }
                        _items.value = currentItems
                    }
                    .onFailure {
                        Log.e("LoadBoardViewModel", "Failed to fetch loads for city $cityId", it)
                    }
            }
        }

        fun onShowMoreClicked(cityId: String) {
            val cityIdInt = cityId.toIntOrNull() ?: return
            onShowMoreClicked(cityIdInt)
        }

        fun onShowMoreClicked(cityId: Int) {
            val currentPage = cityPages[cityId] ?: 1
            val nextPage = currentPage + 1
            cityPages[cityId] = nextPage

            Log.d("LoadBoardViewModel", "Fetching page $nextPage for city $cityId")
            fetchLoadsForCity(cityId, nextPage)
        }

        fun indexOfCity(cityId: String): Int {
            val cityIdInt = cityId.toIntOrNull() ?: return -1
            return indexOfCity(cityIdInt)
        }

        fun indexOfCity(cityId: Int): Int {
            return _items.value.indexOfFirst {
                it is LoadBoardItem.CityItem && it.cityId == cityId
            }
        }

        fun onBookmarkClicked(indentId: String) {
            val targetItem = _items.value.firstOrNull {
                it is LoadBoardItem.LoadItem && it.load.id == indentId
            } as? LoadBoardItem.LoadItem ?: return

            if (targetItem.load.isInterested || targetItem.load.isInterestLoading) return

            interestedLoadIds.add(indentId)
            loadingInterestedLoadIds.add(indentId)
            updateLoadInterestState(indentId, isInterested = true, isLoading = true)

            viewModelScope.launch {
                expressInterestUseCase(ExpressInterestUseCaseParams(indentId))
                    .onSuccess { response ->
                        val success = response.success
                        if (success) {
                            loadingInterestedLoadIds.remove(indentId)
                            updateLoadInterestState(indentId, isInterested = true, isLoading = false)
                        } else {
                            interestedLoadIds.remove(indentId)
                            loadingInterestedLoadIds.remove(indentId)
                            updateLoadInterestState(indentId, isInterested = false, isLoading = false)
                            _bookmarkMessage.emit(response.message.ifBlank { "Unable to express interest right now" })
                        }
                    }.onFailure { error ->
                        interestedLoadIds.remove(indentId)
                        loadingInterestedLoadIds.remove(indentId)
                        updateLoadInterestState(indentId, isInterested = false, isLoading = false)
                        _bookmarkMessage.emit(error.message ?: "Unable to express interest right now")
                    }
            }
        }

        private fun updateLoadInterestState(
            indentId: String,
            isInterested: Boolean,
            isLoading: Boolean,
        ) {
            _items.value = _items.value.map { item ->
                if (item is LoadBoardItem.LoadItem && item.load.id == indentId) {
                    item.copy(
                        load = item.load.copy(
                            isInterested = isInterested,
                            isInterestLoading = isLoading,
                        ),
                    )
                } else {
                    item
                }
            }
        }

        private fun com.freight24.trucker.feature.loads.model.ui.LoadUi.decorateInterestState() =
            copy(
                isInterested = id in interestedLoadIds,
                isInterestLoading = id in loadingInterestedLoadIds,
            )
    }
