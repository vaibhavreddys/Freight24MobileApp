package com.freight24.trucker.feature.profile.view

import com.freight24.trucker.feature.profile.util.ProfileFieldValidators
import com.freight24.trucker.feature.profile.util.ValidationResult

internal fun ifscLookupCode(value: String): String? {
    val code = value.trim().uppercase()
    return code.takeIf { it.length == 11 && ProfileFieldValidators.validateIfsc(it) == ValidationResult.Success }
}
