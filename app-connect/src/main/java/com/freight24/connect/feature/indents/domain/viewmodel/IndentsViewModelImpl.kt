package com.freight24.connect.feature.indents.domain.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class IndentsViewModelImpl
    @Inject
    constructor() : ViewModel(), IndentsViewModel {
        override fun onCreateIndentClick() {
        }
    }
