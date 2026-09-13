package com.freight24.connect.feature.leads.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.repository.LeadsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Supplies the "new + follow-up" badge count for the More tab tile. */
@HiltViewModel
class LeadsBadgeViewModel @Inject constructor(
    private val leadsRepository: LeadsRepository,
) : ViewModel() {

    private val _badgeCount = MutableStateFlow(0)
    val badgeCount = _badgeCount.asStateFlow()

    fun refreshBadge() {
        viewModelScope.launch {
            leadsRepository.getLeadSummary().onSuccess { summary ->
                _badgeCount.value = summary.badgeCount
            }
        }
    }
}
