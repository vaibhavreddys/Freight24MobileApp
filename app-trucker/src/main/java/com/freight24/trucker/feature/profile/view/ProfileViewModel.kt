package com.freight24.trucker.feature.profile.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.trucker.feature.profile.domain.Profile
import com.freight24.trucker.feature.profile.domain.usecase.FetchProfileUseCase
import com.freight24.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
internal class ProfileViewModel @Inject constructor(
    private val fetchProfileUseCase: FetchProfileUseCase,
    private val sessionManager: com.freight24.auth.SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Profile>>(UiState.Loading)
    val uiState: StateFlow<UiState<Profile>> = _uiState.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Editable fields - original values cached from API
    private var originalProfile: Profile? = null

    // Editable field state flows
    val editableFullName = MutableStateFlow("")
    val editableEmailId = MutableStateFlow("")
    val editablePhoneNo = MutableStateFlow("")
    val editableAlternativePhone = MutableStateFlow("")
    val editableBusinessName = MutableStateFlow("")
    val editableGstNumber = MutableStateFlow("")
    val editableAccountHolderName = MutableStateFlow("")
    val editableAccountNumber = MutableStateFlow("")
    val editableIfscCode = MutableStateFlow("")
    val editableUpiId = MutableStateFlow("")
    val editableBankName = MutableStateFlow("")
    val editableBranchName = MutableStateFlow("")

    private val _hasChanges = MutableStateFlow(false)
    val hasChanges: StateFlow<Boolean> = _hasChanges.asStateFlow()

    init {
        fetchProfile()
    }

    fun fetchProfile(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (forceRefresh) {
                _isRefreshing.value = true
            } else {
                _uiState.value = UiState.Loading
            }

            fetchProfileUseCase.invoke(com.freight24.common.usecase.EmptyUseCaseParams)
                .onSuccess { profile ->
                    originalProfile = profile
                    populateEditableFields(profile)
                    _uiState.value = UiState.Success(profile)
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to fetch profile")
                }

            _isRefreshing.value = false
        }
    }

    private fun populateEditableFields(profile: Profile) {
        editableFullName.value = profile.fullName.orEmpty()
        editableEmailId.value = profile.emailId.orEmpty()
        editablePhoneNo.value = profile.phoneNo.orEmpty()
        editableAlternativePhone.value = profile.alternativePhoneNo.firstOrNull().orEmpty()
        editableBusinessName.value = profile.businessName.orEmpty()
        editableGstNumber.value = profile.gstNumber.orEmpty()
        editableAccountHolderName.value = profile.bankAccountDetails?.accountHolderName.orEmpty()
        editableAccountNumber.value = profile.bankAccountDetails?.accountNumber.orEmpty()
        editableIfscCode.value = profile.bankAccountDetails?.ifscCode.orEmpty()
        editableUpiId.value = profile.bankAccountDetails?.upiId.orEmpty()
        editableBankName.value = profile.bankAccountDetails?.bankName.orEmpty()
        editableBranchName.value = profile.bankAccountDetails?.branchName.orEmpty()
    }

    fun toggleEditMode() {
        _isEditing.value = !_isEditing.value
        if (!_isEditing.value) {
            // Exiting edit mode -> reset fields to original values
            originalProfile?.let { populateEditableFields(it) }
            _hasChanges.value = false
        }
    }

    fun onFieldChanged() {
        val original = originalProfile ?: return
        _hasChanges.value = 
            editableFullName.value != (original.fullName.orEmpty()) ||
            editableEmailId.value != (original.emailId.orEmpty()) ||
            editablePhoneNo.value != (original.phoneNo.orEmpty()) ||
            editableAlternativePhone.value != (original.alternativePhoneNo.firstOrNull().orEmpty()) ||
            editableBusinessName.value != (original.businessName.orEmpty()) ||
            editableGstNumber.value != (original.gstNumber.orEmpty()) ||
            editableAccountHolderName.value != (original.bankAccountDetails?.accountHolderName.orEmpty()) ||
            editableAccountNumber.value != (original.bankAccountDetails?.accountNumber.orEmpty()) ||
            editableIfscCode.value != (original.bankAccountDetails?.ifscCode.orEmpty()) ||
            editableUpiId.value != (original.bankAccountDetails?.upiId.orEmpty()) ||
            editableBankName.value != (original.bankAccountDetails?.bankName.orEmpty()) ||
            editableBranchName.value != (original.bankAccountDetails?.branchName.orEmpty())
    }

    fun cancelEdit() {
        _isEditing.value = false
        originalProfile?.let { populateEditableFields(it) }
        _hasChanges.value = false
    }

    fun getUserInitials(): String {
        val name = originalProfile?.fullName ?: return "U"
        return name.split(" ")
            .filter { it.isNotEmpty() }
            .take(2).joinToString("") { it.first().uppercase() }
    }

    fun saveProfile() {
        // TODO: Implement when backend API is available
        // For now, just exit edit mode and keep simulated changes in UI
        _isEditing.value = false
        _hasChanges.value = false
        
        // Simulating success by updating "original" cached values 
        // to match what's in the editable fields (for demo purposes)
        originalProfile?.let { current ->
           originalProfile = current.copy(
               fullName = editableFullName.value,
               emailId = editableEmailId.value,
               phoneNo = editablePhoneNo.value,
               businessName = editableBusinessName.value,
               gstNumber = editableGstNumber.value,
               bankAccountDetails = current.bankAccountDetails?.copy(
                   accountHolderName = editableAccountHolderName.value,
                   accountNumber = editableAccountNumber.value,
                   ifscCode = editableIfscCode.value,
                   upiId = editableUpiId.value,
                   bankName = editableBankName.value,
                   branchName = editableBranchName.value
               )
           )
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.logout()
        }
    }

    fun onRefresh() {
        fetchProfile(forceRefresh = true)
    }

    fun retry() {
        fetchProfile()
    }
}
