package com.freight24.trucker.feature.trucks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TrucksViewModel @Inject constructor() : ViewModel() {

    private val _selectedFilter =
        MutableStateFlow(TruckFilter.ALL)
    val selectedFilter: StateFlow<TruckFilter> = _selectedFilter

    private val allTrucks = dummyTrucks()

    val trucks: StateFlow<List<TruckUiModel>> =
        _selectedFilter.map { filter ->
            when (filter) {
                TruckFilter.ALL -> allTrucks
                TruckFilter.AVAILABLE ->
                    allTrucks.filter { it.status == TruckStatus.AVAILABLE }
                TruckFilter.ON_TRIP ->
                    allTrucks.filter { it.status == TruckStatus.ON_TRIP }
                TruckFilter.MAINTENANCE ->
                    allTrucks.filter { it.status == TruckStatus.MAINTENANCE }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = allTrucks
        )

    fun onFilterSelected(filter: TruckFilter) {
        _selectedFilter.value = filter
    }
}

enum class TruckFilter {
    ALL, AVAILABLE, ON_TRIP, MAINTENANCE
}
