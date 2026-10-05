package com.freight24.connect.feature.leads.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.auth.SessionManager
import com.freight24.connect.repository.LeadsRepository
import com.freight24.rest.request.CallOutcomeRequest
import com.freight24.rest.request.ConvertLeadRequest
import com.freight24.rest.request.LeadRouteEntryRequest
import com.freight24.rest.request.UpdateLeadDetailsRequest
import com.freight24.rest.response.CityResult
import com.freight24.rest.response.LeadDetailDto
import com.freight24.rest.response.LeadTruckTypeDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LeadDetailState(
    val detail: LeadDetailDto? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val actionBusy: String? = null,
    val actionError: String? = null,
    val actionSuccess: String? = null,
    val truckTypes: List<LeadTruckTypeDto> = emptyList(),
    val cityResults: List<CityResult> = emptyList(),
    /**
     * Which city field asked for [cityResults].
     *
     * There is one search per screen, so the results belong to exactly one field. Without
     * this the same list was rendered under every city field at once — typing in Source
     * popped a dropdown under Base Location, Source *and* Destination.
     */
    val cityResultsOwner: String? = null,
    val citySearchLoading: Boolean = false,
    val convertedTagCode: String? = null,
)

@OptIn(FlowPreview::class)
@HiltViewModel
class LeadDetailViewModel
    @Inject
    constructor(
        private val leadsRepository: LeadsRepository,
        savedStateHandle: androidx.lifecycle.SavedStateHandle,
        sessionManager: SessionManager,
    ) : ViewModel() {
        /**
         * Fleet executives are scoped by the backend to their own leads, so "assigned to"
         * always names the person reading the screen. Admins and managers need it.
         */
        val canViewAssignee: Boolean = !sessionManager.isFleetExecutive()

        private val leadId: String = savedStateHandle.get<String>("leadId").orEmpty()

        private val _state = MutableStateFlow(LeadDetailState())
        val state = _state.asStateFlow()

        private var citySearchJob: Job? = null

        init {
            load()
            loadTruckTypes()
        }

        fun load() {
            if (leadId.isBlank()) {
                _state.value = _state.value.copy(isLoading = false, error = "Missing lead id")
                return
            }
            _state.value = _state.value.copy(isLoading = true, error = null)
            viewModelScope.launch {
                leadsRepository.getLeadDetail(leadId)
                    .onSuccess { detail ->
                        _state.value = _state.value.copy(detail = detail, isLoading = false, error = null)
                    }
                    .onFailure { throwable ->
                        _state.value = _state.value.copy(isLoading = false, error = throwable.message ?: "Failed to load lead")
                    }
            }
        }

        private fun loadTruckTypes() {
            viewModelScope.launch {
                leadsRepository.getTruckTypesCached().onSuccess { types ->
                    _state.value = _state.value.copy(truckTypes = types)
                }
            }
        }

        /**
         * Search cities for one specific field.
         *
         * [owner] is the field's stable key (see `LeadDetailScreen`). Results are tagged with
         * it so only that field renders the dropdown.
         */
        fun searchCities(
            owner: String,
            query: String,
        ) {
            citySearchJob?.cancel()
            if (query.length < 2) {
                _state.value =
                    _state.value.copy(
                        cityResults = emptyList(),
                        cityResultsOwner = owner,
                        citySearchLoading = false,
                    )
                return
            }
            _state.value = _state.value.copy(cityResultsOwner = owner, citySearchLoading = true)
            citySearchJob =
                viewModelScope.launch {
                    // Instant cached results first, then live search results
                    val cached = leadsRepository.searchCachedCities(query)
                    if (cached.isNotEmpty()) {
                        _state.value = _state.value.copy(cityResults = cached, citySearchLoading = false)
                    }
                    delay(250)
                    leadsRepository.searchCities(query).onSuccess { live ->
                        _state.value = _state.value.copy(cityResults = live, citySearchLoading = false)
                    }.onFailure {
                        // Keep whatever the cache already gave us; a failed live search must not
                        // wipe suggestions the user can still act on.
                        _state.value = _state.value.copy(citySearchLoading = false)
                    }
                }
        }

        fun clearCityResults() {
            _state.value = _state.value.copy(cityResults = emptyList(), citySearchLoading = false)
        }

        fun recordCallOutcome(
            outcome: String,
            reason: String? = null,
            notes: String? = null,
        ) {
            runAction("call_outcome") {
                val request =
                    when (outcome) {
                        "not_interested" -> CallOutcomeRequest(outcome = outcome, notInterestedReason = reason, notes = notes)
                        "invalid" -> CallOutcomeRequest(outcome = outcome, invalidReason = reason, notes = notes)
                        else -> CallOutcomeRequest(outcome = outcome, notes = notes)
                    }
                leadsRepository.recordCallOutcome(leadId, request)
            }
        }

        fun saveOnboarding(
            name: String,
            baseCityId: Int?,
            truckTypeIds: List<Int>,
            routes: List<LeadRouteEntryRequest>,
            notes: String?,
        ) {
            if (name.isBlank()) {
                _state.value = _state.value.copy(actionError = "Name is required")
                return
            }
            if (truckTypeIds.isEmpty()) {
                _state.value = _state.value.copy(actionError = "Select at least one truck type")
                return
            }
            runAction("save_onboarding") {
                leadsRepository.updateLeadDetails(
                    leadId,
                    UpdateLeadDetailsRequest(
                        name = name,
                        baseCityId = baseCityId,
                        truckTypeIds = truckTypeIds,
                        routes = routes,
                        notes = notes,
                    ),
                )
            }
        }

        fun convertLead() {
            runAction("convert") {
                leadsRepository.convertLead(leadId, ConvertLeadRequest())
            }
        }

        fun reopenLead() {
            runAction("reopen") {
                leadsRepository.reopenLead(leadId)
            }
        }

        fun clearActionMessages() {
            _state.value = _state.value.copy(actionError = null, actionSuccess = null)
        }

        private fun runAction(
            key: String,
            block: suspend () -> Result<*>,
        ) {
            _state.value = _state.value.copy(actionBusy = key, actionError = null, actionSuccess = null)
            viewModelScope.launch {
                block().onSuccess { _state.value = _state.value.copy(actionBusy = null) }
                    .onFailure { throwable ->
                        _state.value =
                            _state.value.copy(
                                actionBusy = null,
                                actionError = throwable.message ?: "Action failed",
                            )
                    }
                // Reload detail to reflect the new status/activity
                leadsRepository.getLeadDetail(leadId).onSuccess { detail ->
                    _state.value = _state.value.copy(detail = detail)
                }
            }
        }
    }
