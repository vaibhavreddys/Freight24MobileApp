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
import com.freight24.trucker.feature.loads.domain.usecase.WithdrawInterestUseCase
import com.freight24.trucker.feature.loads.domain.usecase.WithdrawInterestUseCaseParams
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
        private val withdrawInterestUseCase: WithdrawInterestUseCase,
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

        private var citySummaries: List<com.freight24.common.model.CityLoadSummary> = emptyList()

        // City state trackers
        private val cityPages = mutableMapOf<Int, Int>()
        private val cityLoadedCounts = mutableMapOf<Int, Int>()
        private val cityTotalLoads = mutableMapOf<Int, Int>()
        private val cityLoadCache = mutableMapOf<Int, MutableList<com.freight24.trucker.feature.loads.model.ui.LoadUi>>()
        private val loadingCityIds = mutableSetOf<Int>()

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
            citySummaries = summaryList
            val newItems = mutableListOf<LoadBoardItem>()
            summaryList.forEach { summary ->
                cityTotalLoads[summary.cityId] = summary.totalLoads
                cityLoadCache.putIfAbsent(summary.cityId, summary.initialLoads.map { it.toUi() }.toMutableList())

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
                    val cachedLoads = cityLoadCache[summary.cityId].orEmpty()
                    cityLoadedCounts[summary.cityId] = cachedLoads.size

                    cachedLoads.forEach { load ->
                        newItems.add(LoadBoardItem.LoadItem(load.decorateInterestState()))
                    }

                    // Add Show More if there are more loads
                    if ((cityTotalLoads[summary.cityId] ?: summary.totalLoads) > cachedLoads.size) {
                        newItems.add(
                            LoadBoardItem.ShowMoreItem(
                                cityId = summary.cityId,
                                loadedCount = cachedLoads.size,
                                totalCount = cityTotalLoads[summary.cityId] ?: summary.totalLoads,
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
            val updatedIndex =
                currentItems.indexOfFirst {
                    it is LoadBoardItem.CityItem && it.cityId == cityId
                }

            if (updatedIndex == -1) return

            if (isExpanding) {
                _expandedCityId.value = cityId
                cityPages[cityId] = 1
                insertCachedLoadsForCity(
                    currentItems = currentItems,
                    cityId = cityId,
                    cityHeaderIndex = updatedIndex,
                )
                fetchLoadsForCity(cityId, 1)
            } else {
                _expandedCityId.value = null
                cityPages.remove(cityId)
                // Remove children of THIS city
                val nextIndex = updatedIndex + 1
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
                if (!loadingCityIds.add(cityId)) return@launch

                try {
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

                            if (cityIndex == -1) return@onSuccess

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

                                cityLoadCache[cityId] = mutableListOf()
                                cityLoadedCounts[cityId] = 0
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

                            val cityLoads = cityLoadCache.getOrPut(cityId) { mutableListOf() }
                            val existingIds = cityLoads.map { it.id }.toMutableSet()
                            val newLoads = paginatedLoads.loads.map { it.toUi() }.filter { existingIds.add(it.id) }

                            newLoads.forEach { load ->
                                cityLoads.add(load)
                                currentItems.add(insertIndex++, LoadBoardItem.LoadItem(load.decorateInterestState()))
                            }
                            cityLoadedCounts[cityId] = cityLoads.size
                            cityTotalLoads[cityId] = paginatedLoads.totalLoads
                            cityPages[cityId] = page

                            // Add "Show More" if needed
                            if (paginatedLoads.hasMore) {
                                currentItems.add(
                                    insertIndex,
                                    LoadBoardItem.ShowMoreItem(
                                        cityId = cityId,
                                        loadedCount = cityLoadedCounts[cityId] ?: 0,
                                        totalCount = cityTotalLoads[cityId] ?: paginatedLoads.totalLoads,
                                    ),
                                )
                            }
                            _items.value = currentItems
                        }
                        .onFailure {
                            Log.e("LoadBoardViewModel", "Failed to fetch loads for city $cityId", it)
                        }
                } finally {
                    loadingCityIds.remove(cityId)
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

            if (targetItem.load.isInterestLoading) return

            if (targetItem.load.isInterested) {
                withdrawInterest(indentId)
            } else {
                expressInterest(indentId)
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
            run {
                if (isInterested) interestedLoadIds.add(id)
                copy(
                    isInterested = isInterested || id in interestedLoadIds,
                    isInterestLoading = id in loadingInterestedLoadIds,
                )
            }

        private fun expressInterest(indentId: String) {
            updateInterestLocalState(indentId, isInterested = true, isLoading = true)

            viewModelScope.launch {
                expressInterestUseCase(ExpressInterestUseCaseParams(indentId))
                    .onSuccess { response ->
                        if (response.success) {
                            updateInterestLocalState(indentId, isInterested = true, isLoading = false)
                        } else {
                            updateInterestLocalState(indentId, isInterested = false, isLoading = false)
                            _bookmarkMessage.emit(response.message.ifBlank { "Unable to express interest right now" })
                        }
                    }.onFailure { error ->
                        updateInterestLocalState(indentId, isInterested = false, isLoading = false)
                        _bookmarkMessage.emit(error.message ?: "Unable to express interest right now")
                    }
            }
        }

        private fun withdrawInterest(indentId: String) {
            updateInterestLocalState(indentId, isInterested = false, isLoading = true)

            viewModelScope.launch {
                withdrawInterestUseCase(WithdrawInterestUseCaseParams(indentId))
                    .onSuccess { response ->
                        if (response.success) {
                            updateInterestLocalState(indentId, isInterested = false, isLoading = false)
                        } else {
                            updateInterestLocalState(indentId, isInterested = true, isLoading = false)
                            _bookmarkMessage.emit(response.message.ifBlank { "Unable to withdraw interest right now" })
                        }
                    }.onFailure { error ->
                        updateInterestLocalState(indentId, isInterested = true, isLoading = false)
                        _bookmarkMessage.emit(error.message ?: "Unable to withdraw interest right now")
                    }
            }
        }

        private fun updateInterestLocalState(
            indentId: String,
            isInterested: Boolean,
            isLoading: Boolean,
        ) {
            if (isInterested) interestedLoadIds.add(indentId) else interestedLoadIds.remove(indentId)
            if (isLoading) loadingInterestedLoadIds.add(indentId) else loadingInterestedLoadIds.remove(indentId)
            updateLoadInterestState(indentId, isInterested = isInterested, isLoading = isLoading)
        }

        private fun insertCachedLoadsForCity(
            currentItems: MutableList<LoadBoardItem>,
            cityId: Int,
            cityHeaderIndex: Int,
        ) {
            val insertAt = cityHeaderIndex + 1
            val cachedLoads = cityLoadCache[cityId].orEmpty()
            cityLoadedCounts[cityId] = cachedLoads.size

            var cursor = insertAt
            cachedLoads.forEach { load ->
                currentItems.add(cursor++, LoadBoardItem.LoadItem(load.decorateInterestState()))
            }

            val totalCount = cityTotalLoads[cityId] ?: citySummaries.firstOrNull { it.cityId == cityId }?.totalLoads ?: 0
            if (totalCount > cachedLoads.size) {
                currentItems.add(
                    cursor,
                    LoadBoardItem.ShowMoreItem(
                        cityId = cityId,
                        loadedCount = cachedLoads.size,
                        totalCount = totalCount,
                    ),
                )
            }
        }
    }
