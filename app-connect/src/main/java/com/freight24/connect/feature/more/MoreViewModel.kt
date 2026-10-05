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
    val department: String = "",
    val phone: String = "",
    val email: String = "",
    val status: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class MoreViewModel
    @Inject
    constructor(
        private val sessionManager: SessionManager,
        private val employeesRepository: EmployeesRepository,
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

        /**
         * Cache-first: the More desk is opened repeatedly and basic info does not change
         * within a session, so this reads the Room copy and only calls the API the first
         * time after login (the cache is dropped on logout).
         */
        private fun loadEmployeeBasicInfo() {
            viewModelScope.launch {
                val userId = sessionManager.getUserId()
                if (userId == null) {
                    _employeeInfo.value =
                        _employeeInfo.value.copy(
                            isLoading = false,
                            error = "User ID not found",
                        )
                    return@launch
                }

                // Only show the spinner when there is nothing to show yet.
                _employeeInfo.value =
                    _employeeInfo.value.copy(
                        isLoading = _employeeInfo.value.fullName.isBlank(),
                        error = null,
                    )

                employeesRepository.getEmployeeProfile(userId).fold(
                    onSuccess = { basicInfo ->
                        _employeeInfo.value =
                            EmployeeBasicInfoState(
                                fullName = basicInfo.fullName,
                                designation = basicInfo.designation.orEmpty(),
                                tagCode = basicInfo.tagCode,
                                department = basicInfo.department.orEmpty(),
                                phone = basicInfo.phone.orEmpty(),
                                email = basicInfo.email.orEmpty(),
                                status = basicInfo.status,
                                isLoading = false,
                            )
                    },
                    onFailure = { error ->
                        _employeeInfo.value =
                            _employeeInfo.value.copy(
                                isLoading = false,
                                error = error.localizedMessage ?: "Failed to load employee info",
                            )
                    },
                )
            }
        }

        fun logout() {
            viewModelScope.launch {
                // The cached profile is scoped to the session; drop it so the next login
                // fetches it again instead of serving the previous account's details.
                employeesRepository.clearEmployeeProfileCache()
                sessionManager.logout()
            }
        }
    }
