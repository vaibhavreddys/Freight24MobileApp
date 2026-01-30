package com.freight24.connect.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freight24.connect.repository.FcmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class HomeViewModel
    @Inject
    constructor(
        private val fcmRepository: FcmRepository,
    ) : ViewModel() {
        fun registerDevice() {
            viewModelScope.launch {
                fcmRepository.sendTokenToServerIfNeeded()
            }
        }
    }
