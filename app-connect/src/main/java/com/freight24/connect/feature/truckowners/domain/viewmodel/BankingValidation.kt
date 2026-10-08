package com.freight24.connect.feature.truckowners.domain.viewmodel

import com.freight24.connect.feature.truckowners.domain.usecases.TruckOwnerBankParams
import com.freight24.connect.feature.truckowners.profile.util.ProfileFieldValidators
import com.freight24.connect.feature.truckowners.profile.util.ValidationResult

internal fun TruckOwnerBankParams.hasDetails(): Boolean =
    listOf(accountHolderName, accountNumber, ifscCode, upiId, bankName, branchName).any { !it.isNullOrBlank() } ||
        chequeFile != null

internal fun TruckOwnerBankParams.validationError(): String? {
    if (!hasDetails()) return null
    if (accountHolderName.isBlank()) return "Account holder name is required when providing banking information"
    if (accountNumber.isBlank()) return "Account number is required when providing banking information"
    if (ifscCode.isBlank()) return "IFSC code is required when providing banking information"
    val results = listOf(
        ProfileFieldValidators.validateAccountNumber(accountNumber),
        ProfileFieldValidators.validateIfsc(ifscCode),
    ) + listOfNotNull(upiId?.let { ProfileFieldValidators.validateUpiId(it) })
    return results.filterIsInstance<ValidationResult.Error>().firstOrNull()?.message
}
