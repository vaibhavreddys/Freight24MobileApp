package com.freight24.trucker.feature.loads

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.core.common.repository.LoadBoardRepository
import com.freight24.trucker.feature.loads.mapper.toUi
import com.freight24.trucker.feature.loads.model.LoadBoardItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoadBoardViewModel @Inject constructor(
    private val repository: LoadBoardRepository
) : ViewModel() {

    private val _items = MutableStateFlow<List<LoadBoardItem>>(emptyList())
    val items: StateFlow<List<LoadBoardItem>> = _items.asStateFlow()

    private val _expandedCityId = MutableStateFlow<Int?>(null)
    val expandedCityId: StateFlow<Int?> = _expandedCityId.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Track current page for each city
    private val cityPages = mutableMapOf<Int, Int>()

    init {
        fetchLoadBoardSummary()
    }

    private fun fetchLoadBoardSummary(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            repository.fetchLoadBoardSummary(forceRefresh).collectLatest { summaryList ->
                val newItems = mutableListOf<LoadBoardItem>()
                summaryList.forEach { summary ->
                    // Check if this city was already expanded
                    val isExpanded = _expandedCityId.value == summary.cityId
                    
                    newItems.add(
                        LoadBoardItem.CityItem(
                            cityId = summary.cityId,
                            cityName = summary.cityName,
                            totalLoads = summary.totalLoads,
                            expanded = isExpanded 
                        )
                    )
                }
                _items.value = newItems
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
            try {
                fetchLoadBoardSummary(forceRefresh = true)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun onCityClicked(cityId: String) {
        val cityIdInt = cityId.toIntOrNull() ?: return
        onCityClicked(cityIdInt)
    }

    fun onCityClicked(cityId: Int) {
        val currentItems = _items.value.toMutableList()
        val index = currentItems.indexOfFirst {
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
                val otherIndex = currentItems.indexOfFirst {
                    it is LoadBoardItem.CityItem && it.cityId == otherId
                }
                if (otherIndex != -1) {
                    val otherCity = currentItems[otherIndex] as LoadBoardItem.CityItem
                    // Remove its children
                    val nextIndex = otherIndex + 1
                    while (nextIndex < currentItems.size &&
                        (currentItems[nextIndex] is LoadBoardItem.LoadItem ||
                                currentItems[nextIndex] is LoadBoardItem.ShowMoreItem)
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
                (currentItems[nextIndex] is LoadBoardItem.LoadItem ||
                        currentItems[nextIndex] is LoadBoardItem.ShowMoreItem)
            ) {
                currentItems.removeAt(nextIndex)
            }
        }

        _items.value = currentItems
    }

    private fun fetchLoadsForCity(cityId: Int, page: Int) {
        viewModelScope.launch {
            repository.fetchLoadsForCity(cityId, page).collectLatest { paginatedLoads ->
                val currentItems = _items.value.toMutableList()
                val cityIndex = currentItems.indexOfFirst {
                    it is LoadBoardItem.CityItem && it.cityId == cityId
                }

                if (cityIndex == -1) return@collectLatest

                // Handling Page 1 vs Next Pages
                var insertIndex: Int
                
                if (page == 1) {
                    // --- PAGE 1: REPLACE ALL CHILDREN ---
                    // Find range of existing children and remove them
                    var nextIndex = cityIndex + 1
                    while (nextIndex < currentItems.size &&
                        (currentItems[nextIndex] is LoadBoardItem.LoadItem ||
                                currentItems[nextIndex] is LoadBoardItem.ShowMoreItem)
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
                        (currentItems[nextIndex] is LoadBoardItem.LoadItem ||
                                currentItems[nextIndex] is LoadBoardItem.ShowMoreItem)
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
                    currentItems.add(insertIndex++, LoadBoardItem.LoadItem(load.toUi()))
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
                            totalCount = paginatedLoads.totalLoads
                        )
                    )
                }

                _items.value = currentItems
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
}
