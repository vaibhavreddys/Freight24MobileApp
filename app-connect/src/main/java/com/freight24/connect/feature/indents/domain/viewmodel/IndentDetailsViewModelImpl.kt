package com.freight24.connect.feature.indents.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.feature.indents.domain.usecases.GetIndentByIdUseCase
import com.freight24.connect.feature.indents.domain.usecases.GetIndentByIdUseCaseParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IndentDetailsViewModelImpl @Inject constructor(
    private val getIndentByIdUseCase: GetIndentByIdUseCase
) : ViewModel(), IndentDetailsViewModel {

    private val _uiState = MutableStateFlow(IndentDetailsUiState())
    override val uiState: StateFlow<IndentDetailsUiState> = _uiState.asStateFlow()

    private var currentIndentId: String? = null

    override fun onEvent(event: IndentDetailsEvent) {
        when (event) {
            is IndentDetailsEvent.LoadIndent -> {
                currentIndentId = event.indentId
                loadIndent(event.indentId)
            }
            is IndentDetailsEvent.Refresh -> {
                currentIndentId?.let { loadIndent(it) }
            }
            is IndentDetailsEvent.ClearError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun loadIndent(indentId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = getIndentByIdUseCase.invoke(
                GetIndentByIdUseCaseParams(identId = indentId)
            )

            if (result.isSuccess) {
                val response = result.getOrNull()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        indent = response?.data
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to load indent details"
                    )
                }
            }
        }
    }
}
