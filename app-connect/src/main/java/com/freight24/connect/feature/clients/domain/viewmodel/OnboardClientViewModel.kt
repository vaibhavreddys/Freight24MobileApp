package com.freight24.connect.feature.clients.domain.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.common.util.FileUtils
import com.freight24.connect.feature.clients.domain.usecases.ClientBranchV2Params
import com.freight24.connect.feature.clients.domain.usecases.ClientContactV2Params
import com.freight24.connect.feature.clients.domain.usecases.ClientContactScopeV2Params
import com.freight24.connect.feature.clients.domain.usecases.ClientLocationsV2Params
import com.freight24.connect.feature.clients.domain.usecases.ClientProfileV2Params
import com.freight24.connect.feature.clients.domain.usecases.CreateClientUseCase
import com.freight24.connect.repository.ClientRepository
import com.freight24.connect.feature.truckowners.profile.util.ProfileFieldValidators
import com.freight24.connect.feature.truckowners.profile.util.ValidationResult
import com.freight24.rest.api.ClientApi
import com.freight24.rest.response.BranchTypeOptionDto
import com.freight24.rest.response.CityOptionDto
import com.freight24.rest.response.ContactRoleOptionDto
import com.freight24.rest.response.StateOptionDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class OnboardClientViewModel @Inject constructor(
    private val createClientUseCase: CreateClientUseCase,
    private val repository: ClientRepository,
    private val clientApi: ClientApi,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private companion object {
        const val CLIENT_ID_KEY = "client_onboarding_client_id"
        const val USER_ID_KEY = "client_onboarding_user_id"
        const val STEP_KEY = "client_onboarding_step"
    }

    val clientType = MutableStateFlow("DIRECT_CUSTOMER")
    val companyType = MutableStateFlow("Private Limited")
    val operatingStructure = MutableStateFlow("SINGLE")
    val fullName = MutableStateFlow("")
    val phoneNumber = MutableStateFlow("")
    val panNumber = MutableStateFlow("")
    val email = MutableStateFlow("")
    val companyName = MutableStateFlow("")
    val gstNumber = MutableStateFlow("")
    val branches = MutableStateFlow(listOf(BranchFormState(id = 1)))

    val panFile = MutableStateFlow<File?>(null)
    val gstFile = MutableStateFlow<File?>(null)
    val companyRegistrationFile = MutableStateFlow<File?>(null)
    val contractFile = MutableStateFlow<File?>(null)

    private val _states = MutableStateFlow<List<StateOptionDto>>(emptyList())
    val states: StateFlow<List<StateOptionDto>> = _states.asStateFlow()
    private val _citiesByState = MutableStateFlow<Map<Int, List<CityOptionDto>>>(emptyMap())
    val citiesByState: StateFlow<Map<Int, List<CityOptionDto>>> = _citiesByState.asStateFlow()
    private val _branchTypes = MutableStateFlow<List<BranchTypeOptionDto>>(emptyList())
    val branchTypes: StateFlow<List<BranchTypeOptionDto>> = _branchTypes.asStateFlow()
    private val _contactRoles = MutableStateFlow<List<ContactRoleOptionDto>>(emptyList())
    val contactRoles: StateFlow<List<ContactRoleOptionDto>> = _contactRoles.asStateFlow()

    private var nextBranchId = 2
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()
    private val _completedStep = MutableStateFlow(savedStateHandle[STEP_KEY] ?: 0)
    val completedStep: StateFlow<Int> = _completedStep.asStateFlow()
    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent: SharedFlow<String> = _messageEvent.asSharedFlow()
    private val _createdClient = MutableStateFlow<CreatedClientState?>(null)
    val createdClient: StateFlow<CreatedClientState?> = _createdClient.asStateFlow()
    private val _panDuplicateError = MutableStateFlow<String?>(null)
    val panDuplicateError: StateFlow<String?> = _panDuplicateError.asStateFlow()
    private val _gstDuplicateError = MutableStateFlow<String?>(null)
    val gstDuplicateError: StateFlow<String?> = _gstDuplicateError.asStateFlow()

    init {
        loadMasterData()
    }

    private fun loadMasterData() {
        viewModelScope.launch {
            repository.getStates().onSuccess { _states.value = it }
            repository.getBranchTypes().onSuccess { types ->
                _branchTypes.value = types
                val defaultType = types.firstOrNull { it.code == "HEAD_OFFICE" } ?: types.firstOrNull()
                if (defaultType != null) branches.update { list -> list.map { it.copy(branchTypeId = defaultType.id) } }
            }
            repository.getContactRoles().onSuccess { _contactRoles.value = it }
        }
    }

    fun onClientTypeChanged(value: String) { clientType.value = value }
    fun onCompanyTypeChanged(value: String) { companyType.value = value }
    fun onOperatingStructureChanged(value: String) { operatingStructure.value = value }
    fun onFullNameChanged(input: String) { fullName.value = sanitizePersonName(input) }
    fun onPhoneNumberChanged(input: String) { phoneNumber.value = input.filter(Char::isDigit).take(10) }
    fun onEmailChanged(input: String) { email.value = input.lowercase().filter { it.isLetterOrDigit() || it in "@._+-" }.take(80) }
    fun onCompanyNameChanged(input: String) { companyName.value = sanitizeText(input, 100) }

    fun onPanNumberChanged(input: String) {
        val value = input.uppercase().filter(Char::isLetterOrDigit).take(10)
        if (isPanPrefix(value)) panNumber.value = value
        _panDuplicateError.value = null
    }

    fun onGstNumberChanged(input: String) {
        val value = input.uppercase().filter(Char::isLetterOrDigit).take(15)
        if (isGstPrefix(value)) gstNumber.value = value
        _gstDuplicateError.value = null
    }

    fun onPanFocusLost() = checkDuplicate("pan_card", panNumber.value, _panDuplicateError, "PAN already registered with another client")
    fun onGstFocusLost() {
        if (gstNumber.value.isNotBlank()) checkDuplicate("gst_number", gstNumber.value, _gstDuplicateError, "GST already registered with another client")
    }

    private fun checkDuplicate(field: String, value: String, target: MutableStateFlow<String?>, message: String) {
        if ((field == "pan_card" && value.length != 10) || (field == "gst_number" && value.length != 15)) return
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { clientApi.checkClientField(field, value) }.getOrNull()?.let { response ->
                if (response.isSuccessful && response.body()?.exists == true) target.value = message
            }
        }
    }

    fun addBranch() {
        val defaultType = _branchTypes.value.firstOrNull()?.id.orEmpty()
        branches.update { it + BranchFormState(id = nextBranchId++, branchTypeId = defaultType) }
    }

    fun onBranchNameChanged(id: Int, value: String) = updateBranch(id) { it.copy(branchName = sanitizeText(value, 80)) }
    fun onBranchCodeChanged(id: Int, value: String) = updateBranch(id) { it.copy(branchCode = value.uppercase().filter { c -> c.isLetterOrDigit() || c == '-' }.take(20)) }
    fun onBranchAddressChanged(id: Int, value: String) = updateBranch(id) { it.copy(branchAddress = sanitizeAddress(value)) }
    fun onBranchPinCodeChanged(id: Int, value: String) = updateBranch(id) { it.copy(pinCode = value.filter(Char::isDigit).take(6)) }
    fun onBranchTypeChanged(id: Int, typeId: String) = updateBranch(id) { it.copy(branchTypeId = typeId) }

    fun onBranchStateChanged(id: Int, stateId: Int) {
        updateBranch(id) { it.copy(stateId = stateId, cityId = null) }
        if (_citiesByState.value[stateId] == null) {
            viewModelScope.launch { repository.getCities(stateId).onSuccess { cities -> _citiesByState.update { it + (stateId to cities) } } }
        }
    }

    fun onBranchCityChanged(id: Int, cityId: Int) = updateBranch(id) { it.copy(cityId = cityId) }
    fun onPaymentContactNameChanged(id: Int, value: String) = updateBranch(id) { it.copy(paymentContactName = sanitizePersonName(value)) }
    fun onPaymentContactNumberChanged(id: Int, value: String) = updateBranch(id) { it.copy(paymentContactNumber = phoneDigits(value)) }
    fun onTrafficContactNameChanged(id: Int, value: String) = updateBranch(id) { it.copy(trafficContactName = sanitizePersonName(value)) }
    fun onTrafficContactNumberChanged(id: Int, value: String) = updateBranch(id) { it.copy(trafficContactNumber = phoneDigits(value)) }
    fun onPodContactNameChanged(id: Int, value: String) = updateBranch(id) { it.copy(podContactName = sanitizePersonName(value)) }
    fun onPodContactNumberChanged(id: Int, value: String) = updateBranch(id) { it.copy(podContactNumber = phoneDigits(value)) }
    fun onTrackingContactNameChanged(id: Int, value: String) = updateBranch(id) { it.copy(trackingContactName = sanitizePersonName(value)) }
    fun onTrackingContactNumberChanged(id: Int, value: String) = updateBranch(id) { it.copy(trackingContactNumber = phoneDigits(value)) }
    fun copyPaymentFromPrimary(id: Int) = updateBranch(id) { it.copy(paymentContactName = fullName.value, paymentContactNumber = phoneNumber.value) }
    fun copyTrafficFromPrimary(id: Int) = updateBranch(id) { it.copy(trafficContactName = fullName.value, trafficContactNumber = phoneNumber.value) }
    fun copyPodFromPrimary(id: Int) = updateBranch(id) { it.copy(podContactName = fullName.value, podContactNumber = phoneNumber.value) }
    fun copyTrackingFromPrimary(id: Int) = updateBranch(id) { it.copy(trackingContactName = fullName.value, trackingContactNumber = phoneNumber.value) }

    fun onFileSelected(context: Context, uris: List<Uri>, type: String, isPdf: Boolean, onSuccess: () -> Unit) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val file = if (isPdf) FileUtils.getFileFromUri(context, uris.first()) else FileUtils.convertImageToPdf(context, uris)
                if (file == null) {
                    _messageEvent.emit("Failed to process document")
                } else {
                    when (type) {
                        "PAN" -> panFile.value = file
                        "GST" -> gstFile.value = file
                        "COMPANY" -> companyRegistrationFile.value = file
                        "CONTRACT" -> contractFile.value = file
                    }
                    onSuccess()
                }
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun canSubmit(): Boolean = !_isSaving.value && validationMessage() == null
    fun submitBlockReason(): String? = validationMessage()
    fun clearDuplicateErrors() { _panDuplicateError.value = null; _gstDuplicateError.value = null }

    fun validationMessage(): String? {
        val registered = branches.value.firstOrNull() ?: return "Registered address is required"
        if (_completedStep.value < 1) {
            if (companyName.value.isBlank()) return "Legal company name is required"
            if (fullName.value.isBlank()) return "Display or primary contact name is required"
            if (companyType.value.isBlank()) return "Company type is required"
            if (panNumber.value.isBlank()) return "PAN number is required"
            if (phoneNumber.value.isBlank()) return "Company phone is required"
            if (registered.branchAddress.isBlank()) return "Registered address is required"
            if (registered.stateId == null) return "Registered state is required"
            if (registered.cityId == null) return "Registered city is required"
            if (!registered.pinCode.matches(Regex("^[1-9][0-9]{5}$"))) return "Enter a valid registered PIN code"
            if (_panDuplicateError.value != null) return _panDuplicateError.value
            if (_gstDuplicateError.value != null) return _gstDuplicateError.value
            when (val result = ProfileFieldValidators.validatePan(panNumber.value)) {
                is ValidationResult.Error -> return result.message
                ValidationResult.Success -> Unit
            }
            if (gstNumber.value.isNotBlank()) when (val result = ProfileFieldValidators.validateGst(gstNumber.value)) {
                is ValidationResult.Error -> return result.message
                ValidationResult.Success -> Unit
            }
        }
        if (operatingStructure.value == "MULTIPLE" && _completedStep.value < 2) {
            branches.value.forEachIndexed { index, branch ->
                if (branch.branchName.isBlank()) return "Branch ${index + 1}: name is required"
                if (branch.branchTypeId.isBlank()) return "Branch ${index + 1}: type is required"
                if (branch.branchAddress.isBlank() || branch.stateId == null || branch.cityId == null) return "Branch ${index + 1}: complete address is required"
                if (!branch.pinCode.matches(Regex("^[1-9][0-9]{5}$"))) return "Branch ${index + 1}: valid PIN code is required"
            }
        }
        if (operatingStructure.value == "SINGLE" && _completedStep.value < 2 && registered.branchName.isBlank()) {
            return "Head office name is required"
        }
        if (_contactRoles.value.isEmpty()) return "Contact roles are still loading"
        if (fullName.value.isBlank()) return "Primary contact name is required"
        if (phoneNumber.value.isBlank()) return "Primary contact phone is required"
        when (val result = ProfileFieldValidators.validateIndianPhone(phoneNumber.value)) {
            is ValidationResult.Error -> return result.message
            ValidationResult.Success -> Unit
        }
        if (email.value.isNotBlank()) when (val result = ProfileFieldValidators.validateEmail(email.value)) {
            is ValidationResult.Error -> return result.message
            ValidationResult.Success -> Unit
        }
        contactPhoneValues().forEach { if (it.isNotBlank() && it.length != 10) return "Contact phone must be 10 digits" }
        return null
    }

    fun submit() {
        if (_isSaving.value) return
        validationMessage()?.let { error -> viewModelScope.launch { _messageEvent.emit(error) }; return }
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val registered = branches.value.first()
                var clientId: String? = savedStateHandle[CLIENT_ID_KEY]
                var userId: String? = savedStateHandle[USER_ID_KEY]
                if (_completedStep.value < 1) {
                    val created = createClientUseCase.createProfile(
                        ClientProfileV2Params(
                            clientType.value, companyName.value.trim(), fullName.value.trim(), companyType.value,
                            panNumber.value, gstNumber.value.ifBlank { null }, email.value.ifBlank { null }, apiPhone(phoneNumber.value),
                            registered.branchAddress, requireNotNull(registered.cityId), requireNotNull(registered.stateId), registered.pinCode,
                            panFile.value, gstFile.value, companyRegistrationFile.value, contractFile.value,
                        ),
                    ).getOrElse { _messageEvent.emit(it.message ?: "Failed to create client profile"); return@launch }
                    clientId = created.clientId
                    userId = created.userId
                    savedStateHandle[CLIENT_ID_KEY] = clientId
                    savedStateHandle[USER_ID_KEY] = userId
                    setStep(1)
                }

                val resolvedClientId = requireNotNull(clientId)
                var createdBranches = emptyList<com.freight24.rest.response.ClientOnboardingBranchDto>()
                if (_completedStep.value < 2) {
                    createdBranches = createClientUseCase.saveLocations(
                        resolvedClientId,
                        ClientLocationsV2Params(
                            operatingStructure.value,
                            branches.value.first().branchName.ifBlank { "${companyName.value} - Head Office" },
                            if (operatingStructure.value == "MULTIPLE") {
                                branches.value.map { branch ->
                                    ClientBranchV2Params(
                                        branch.branchName, branch.branchCode.ifBlank { null }, branch.branchTypeId,
                                        branch.branchAddress, requireNotNull(branch.cityId), requireNotNull(branch.stateId), branch.pinCode,
                                        null,
                                    )
                                }
                            } else {
                                emptyList()
                            },
                        ),
                    ).getOrElse { _messageEvent.emit("Client profile saved. ${it.message ?: "Failed to save locations"}"); return@launch }
                    savedStateHandle["client_onboarding_branches"] = ArrayList(createdBranches.map { it.id })
                    setStep(2)
                }

                val branchIds = createdBranches.map { it.id }.ifEmpty { savedStateHandle.get<ArrayList<String>>("client_onboarding_branches").orEmpty() }
                val contacts = runCatching { buildContacts(branchIds) }.getOrElse {
                    _messageEvent.emit(it.message ?: "Failed to prepare client contacts")
                    return@launch
                }
                contacts.forEach { contact ->
                    createClientUseCase.createContact(resolvedClientId, contact).getOrElse {
                        _messageEvent.emit("Client and locations saved. ${it.message ?: "Failed to save contacts"}")
                        return@launch
                    }
                }

                val resolvedUserId = requireNotNull(userId)
                _createdClient.value = CreatedClientState(companyName.value.trim(), resolvedUserId)
                clearProgress()
                _messageEvent.emit("Client created successfully")
            } finally {
                _isSaving.value = false
            }
        }
    }

    private fun buildContacts(branchIds: List<String>): List<ClientContactV2Params> {
        data class Draft(val name: String, val phone: String, val code: String, val branchIndex: Int)
        val drafts = mutableListOf<Draft>()
        branches.value.forEachIndexed { index, branch ->
            listOf(
                Draft(branch.paymentContactName, branch.paymentContactNumber, "PAYMENT", index),
                Draft(branch.trafficContactName, branch.trafficContactNumber, "OPERATIONS", index),
                Draft(branch.podContactName, branch.podContactNumber, "POD", index),
                Draft(branch.trackingContactName, branch.trackingContactNumber, "TRACKING", index),
            ).filterTo(drafts) { it.name.isNotBlank() && it.phone.isNotBlank() }
        }
        if (drafts.isEmpty()) drafts += Draft(fullName.value, phoneNumber.value, "OPERATIONS", 0)
        return drafts.groupBy { "${it.phone}:${it.name}" }.map { (_, group) ->
            val scopeGroups = if (operatingStructure.value == "SKIP") mapOf(-1 to group) else group.groupBy { it.branchIndex }
            val assignments = scopeGroups.map { (branchIndex, scopedDrafts) ->
                val branchId = if (branchIndex < 0) null else branchIds.getOrNull(branchIndex)
                val applicable = if (branchId == null) setOf("CLIENT", "BOTH") else setOf("BRANCH", "BOTH")
                val roles = _contactRoles.value.filter { it.applicableTo.uppercase() in applicable }
                val roleIds = scopedDrafts.mapNotNull { draft ->
                    roles.firstOrNull { role -> role.code.uppercase() == draft.code || role.code.uppercase().contains(draft.code) }?.id
                }.distinct().ifEmpty { listOfNotNull(roles.firstOrNull()?.id) }
                if (roleIds.isEmpty()) error("No active contact role is configured for ${if (branchId == null) "client" else "branch"} contacts")
                ClientContactScopeV2Params(branchId = branchId, roleIds = roleIds)
            }
            ClientContactV2Params(
                fullName = group.first().name,
                phone = apiPhone(group.first().phone).orEmpty(),
                isPrimary = group.first().phone == phoneNumber.value,
                assignments = assignments,
            )
        }
    }

    private fun setStep(step: Int) { _completedStep.value = step; savedStateHandle[STEP_KEY] = step }
    private fun clearProgress() {
        savedStateHandle.remove<String>(CLIENT_ID_KEY)
        savedStateHandle.remove<String>(USER_ID_KEY)
        savedStateHandle.remove<Int>(STEP_KEY)
        savedStateHandle.remove<ArrayList<String>>("client_onboarding_branches")
        _completedStep.value = 0
    }

    fun consumeCreatedClient() { _createdClient.value = null }
    private fun updateBranch(id: Int, update: (BranchFormState) -> BranchFormState) { branches.update { list -> list.map { if (it.id == id) update(it) else it } } }
    private fun contactPhoneValues() = branches.value.flatMap { listOf(it.paymentContactNumber, it.trafficContactNumber, it.podContactNumber, it.trackingContactNumber) }
    private fun phoneDigits(value: String) = value.filter(Char::isDigit).take(10)
    private fun apiPhone(value: String): String? = value.trim().ifBlank { null }?.let { if (it.length == 10) "+91$it" else it }
    private fun sanitizePersonName(value: String) = value.filter { it.isLetter() || it == ' ' || it == '.' }.replace(Regex("\\s+"), " ").take(60).trimStart()
    private fun sanitizeText(value: String, max: Int) = value.filter { it.isLetterOrDigit() || it in " .,&()-/" }.replace(Regex("\\s+"), " ").take(max).trimStart()
    private fun sanitizeAddress(value: String) = value.filter { it.isLetterOrDigit() || it in " .,&()/#-," }.replace(Regex("[ ]{2,}"), " ").take(180).trimStart()
    private fun isPanPrefix(value: String) = value.withIndex().all { (i, c) -> if (i in 5..8) c.isDigit() else c.isLetter() }
    private fun isGstPrefix(value: String) = value.withIndex().all { (i, c) -> when { i in 0..1 -> c.isDigit(); i in 2..6 || i == 11 -> c.isLetter(); i in 7..10 -> c.isDigit(); i == 13 -> c == 'Z'; else -> c.isLetterOrDigit() } }

    data class BranchFormState(
        val id: Int,
        val branchName: String = "",
        val branchCode: String = "",
        val branchTypeId: String = "",
        val branchAddress: String = "",
        val stateId: Int? = null,
        val cityId: Int? = null,
        val pinCode: String = "",
        val paymentContactName: String = "",
        val paymentContactNumber: String = "",
        val trafficContactName: String = "",
        val trafficContactNumber: String = "",
        val podContactName: String = "",
        val podContactNumber: String = "",
        val trackingContactName: String = "",
        val trackingContactNumber: String = "",
    )

    data class CreatedClientState(val fullName: String, val userId: String?)
}
