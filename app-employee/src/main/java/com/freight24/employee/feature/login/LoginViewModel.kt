package com.freight24.employee.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel
    @Inject
    constructor() : ViewModel() {
        private val _phoneNumber = MutableStateFlow("")
        val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

        fun onPhoneNumberChange(newValue: String) {
            // Simple validation to allow only digits and max length if needed
            if (newValue.all { it.isDigit() } && newValue.length <= 10) {
                _phoneNumber.value = newValue
            }
        }

        fun onSendOtpClick() {
            viewModelScope.launch {
                val phone = _phoneNumber.value
                if (phone.length == 10) {
                    // TODO: Call Trucker Login API
                    println("Initiating Trucker Login for $phone")
                } else {
                    // TODO: Show error state
                }
            }
        }
    }
