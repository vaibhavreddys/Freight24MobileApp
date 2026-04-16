package com.freight24.connect.feature.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.auth.SessionManager
import com.freight24.connect.repository.EmployeesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmployeeBasicInfoState(
    val fullName: String = "",
    val designation: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MoreViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val employeesRepository: EmployeesRepository
) : ViewModel() {

    private val _navigateToLogin = MutableSharedFlow<Unit>()
    val navigateToLogin = _navigateToLogin.asSharedFlow()

    private val _employeeInfo = MutableStateFlow(EmployeeBasicInfoState())
    val employeeInfo = _employeeInfo.asStateFlow()

    init {
        loadEmployeeBasicInfo()
    }

    private fun loadEmployeeBasicInfo() {
        viewModelScope.launch {
            _employeeInfo.value = _employeeInfo.value.copy(isLoading = true, error = null)
            try {
                val userId = sessionManager.getUserId()
                if (userId != null) {
                    employeesRepository.getEmployeeBasicInfo(userId).fold(
                        onSuccess = { basicInfo ->
                            _employeeInfo.value = EmployeeBasicInfoState(
                                fullName = basicInfo.fullName,
                                designation = basicInfo.designation,
                                isLoading = false
                            )
                        },
                        onFailure = { error ->
                            _employeeInfo.value = _employeeInfo.value.copy(
                                isLoading = false,
                                error = error.localizedMessage ?: "Failed to load employee info"
                            )
                        }
                    )
                } else {
                    _employeeInfo.value = _employeeInfo.value.copy(
                        isLoading = false,
                        error = "User ID not found"
                    )
                }
            } catch (e: Exception) {
                _employeeInfo.value = _employeeInfo.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Failed to load employee info"
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.logout()
            _navigateToLogin.emit(Unit)
        }
    }
}
