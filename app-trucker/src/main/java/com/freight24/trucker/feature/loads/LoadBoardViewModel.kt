package com.freight24.trucker.feature.loads

import androidx.lifecycle.ViewModel
import com.freight24.trucker.feature.loads.data.DummyLoadData
import com.freight24.trucker.feature.loads.model.LoadBoardItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.collections.emptyList

class LoadBoardViewModel : ViewModel() {

    private val pageSize = 10
    private val _expandedCityId = MutableStateFlow<String?>(null)
    var expandedCityId: StateFlow<String?> = _expandedCityId
    private var loadedCount = 0

    private val cities = listOf(
        "Mumbai", "Pune", "Bangalore", "Hyderabad", "Chennai"
    )

    private val _items = MutableStateFlow<List<LoadBoardItem>>(initialCityItems())
    val items: StateFlow<List<LoadBoardItem>> = _items

    private fun initialCityItems(): List<LoadBoardItem> =
        cities.map {
            LoadBoardItem.CityItem(
                cityId = it,
                cityName = it,
                totalLoads = 120,
                expanded = false
            )
        }

    fun onCityClicked(cityId: String) {
        if (_expandedCityId.value == cityId) {
            collapse()
        } else {
            expand(cityId)
        }
    }

    fun onShowMoreClicked(cityId: String) {
        val nextLoads = DummyLoadData.loadsForCity(
            city = cityId,
            start = loadedCount,
            count = pageSize
        )

        loadedCount += pageSize
        _items.value = _items.value.flatMap { item ->
            when (item) {
                is LoadBoardItem.ShowMoreItem -> {
                    if (item.cityId == cityId) {
                        buildList<LoadBoardItem> {
                            // Add newly loaded loads
                            addAll(
                                nextLoads.map { load ->
                                    LoadBoardItem.LoadItem(load)
                                }
                            )
                            // Add ShowMore again if needed
                            if (loadedCount < item.totalCount) {
                                add(
                                    LoadBoardItem.ShowMoreItem(
                                        cityId = cityId,
                                        loadedCount = loadedCount,
                                        totalCount = item.totalCount
                                    )
                                )
                            }
                        }
                    } else {
                        listOf(item)
                    }
                }
                else -> listOf(item)
            }
        }
    }

    private fun expand(cityId: String) {
        _expandedCityId.value = cityId
        loadedCount = pageSize

        val loads = DummyLoadData.loadsForCity(cityId, 0, pageSize)

        _items.value = cities.flatMap { city ->
            if (city == cityId) {
                listOf(
                    LoadBoardItem.CityItem(city, city, 120, true)
                ) +
                        loads.map { LoadBoardItem.LoadItem(it) } +
                        LoadBoardItem.ShowMoreItem(city, loadedCount, 120)
            } else {
                listOf(
                    LoadBoardItem.CityItem(city, city, 120, false)
                )
            }
        }
    }

    private fun collapse() {
        _expandedCityId.value = null
        loadedCount = 0
        _items.value = initialCityItems()
    }

    fun indexOfCity(cityId: String): Int {
        return _items.value.indexOfFirst {
            it is LoadBoardItem.CityItem && it.cityId == cityId
        }
    }

}
