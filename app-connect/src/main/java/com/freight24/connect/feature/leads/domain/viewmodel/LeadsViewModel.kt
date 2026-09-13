package com.freight24.connect.feature.leads.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.repository.LeadsRepository
import com.freight24.rest.response.LeadDto
import com.freight24.rest.response.LeadSummaryData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LeadsTab(val label: String, val statuses: String?) {
    NEW("New", "new"),
    FOLLOW_UP("Follow-up", "contact_in_progress,follow_up"),
    COMPLETED("Completed", "soft_onboarded,converted"),
    NOT_INTERESTED("Not Interested", "not_interested,invalid,do_not_contact"),
}

@HiltViewModel
class LeadsViewModel @Inject constructor(
    private val leadsRepository: LeadsRepository,
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(LeadsTab.NEW)
    val selectedTab = _selectedTab.asStateFlow()

    private val _leads = MutableStateFlow<List<LeadDto>>(emptyList())
    val leads = _leads.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore = _isLoadingMore.asStateFlow()

    private val _hasMore = MutableStateFlow(false)
    val hasMore = _hasMore.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _summary = MutableStateFlow<LeadSummaryData?>(null)
    val summary = _summary.asStateFlow()

    private var currentPage = 1
    private var totalPages = 1

    init {
        refresh()
        loadSummary()
    }

    /** Live status counts so each tab chip shows its workload at a glance. */
    fun loadSummary() {
        viewModelScope.launch {
            leadsRepository.getLeadSummary().onSuccess { _summary.value = it }
        }
    }

    fun countForTab(tab: LeadsTab): Int {
        val counts = _summary.value?.statusCounts ?: return 0
        return tab.statuses?.split(',')
            ?.sumOf { status -> counts[status.trim()] ?: 0 }
            ?: counts.values.sum()
    }

    fun setTab(tab: LeadsTab) {
        if (_selectedTab.value == tab) return
        _selectedTab.value = tab
        refresh()
    }

    fun refresh() {
        currentPage = 1
        totalPages = 1
        _hasMore.value = false
        loadPage(reset = true, showSpinner = _leads.value.isEmpty())
    }

    fun loadMore() {
        if (_isLoadingMore.value || _isRefreshing.value || !_hasMore.value) return
        loadPage(reset = false, showSpinner = false)
    }

    private fun loadPage(reset: Boolean, showSpinner: Boolean) {
        val page = if (reset) 1 else currentPage + 1
        if (reset) {
            if (showSpinner) _isLoading.value = true else _isRefreshing.value = true
        } else {
            _isLoadingMore.value = true
        }
        _error.value = null

        viewModelScope.launch {
            val result = leadsRepository.getLeads(
                status = _selectedTab.value.statuses,
                page = page,
                limit = PAGE_SIZE,
            )
            result.onSuccess { leadsPage ->
                currentPage = leadsPage.page
                totalPages = leadsPage.totalPages
                _hasMore.value = leadsPage.page < leadsPage.totalPages
                _leads.value = if (reset) leadsPage.leads else _leads.value + leadsPage.leads
            }.onFailure { throwable ->
                _error.value = throwable.message ?: "Failed to load leads"
            }
            if (reset) loadSummary()
            _isLoading.value = false
            _isRefreshing.value = false
            _isLoadingMore.value = false
        }
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}
