package com.freight24.connect.feature.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.auth.SessionManager
import com.freight24.connect.repository.EmployeesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmployeeBasicInfoState(
    val fullName: String = "",
    val designation: String = "",
    val tagCode: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MoreViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val employeesRepository: EmployeesRepository
) : ViewModel() {

    private val _employeeInfo = MutableStateFlow(EmployeeBasicInfoState())
    val employeeInfo = _employeeInfo.asStateFlow()

    private val _showLogoutSheet = MutableStateFlow(false)
    val showLogoutSheet = _showLogoutSheet.asStateFlow()

    fun requestLogout() {
        _showLogoutSheet.value = true
    }

    fun dismissLogoutSheet() {
        _showLogoutSheet.value = false
    }

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
                                tagCode = basicInfo.tagCode,
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
        }
    }
}
