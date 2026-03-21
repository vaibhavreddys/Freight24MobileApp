package com.freight24.connect.feature.inbox.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.common.usecase.EmptyUseCaseParams
import com.freight24.connect.feature.inbox.domain.usecase.InboxUseCase
import com.freight24.connect.feature.inbox.domain.usecase.InboxUseCaseParams
import com.freight24.connect.feature.inbox.domain.usecase.WithDrawInterestUseCaseParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class InboxViewModelImpl @Inject constructor(
    private val inboxUseCase: InboxUseCase,
    private val withdrawInterestUseCase: com.freight24.connect.feature.inbox.domain.usecase.WithDrawInterestUseCase
) : ViewModel(), InboxViewModel {

    private val _uiState = MutableStateFlow(InboxUiState())
    override val uiState: StateFlow<InboxUiState> = _uiState.asStateFlow()

    init {
        loadInbox()
    }

    override fun onEvent(event: InboxEvent) {
        when (event) {
            InboxEvent.Refresh -> loadInbox()
            is InboxEvent.RejectInterest -> rejectInterest(event)
        }
    }

    private fun rejectInterest(event: InboxEvent.RejectInterest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val params = WithDrawInterestUseCaseParams(
                interestId = event.interestId,
                remarks = event.remarks
            )
            val result = withdrawInterestUseCase(params)
            result.fold(
                onSuccess = {
                    loadInbox() // Refresh on success
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to reject interest"
                        )
                    }
                }
            )
        }
    }

    private fun loadInbox() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = inboxUseCase(InboxUseCaseParams(null))
            result.fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            data = response.data,
                            totalIndents = response.totalIndents
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load inbox"
                        )
                    }
                }
            )
        }
    }
}
