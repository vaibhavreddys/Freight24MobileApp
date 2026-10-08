package com.freight24.connect.feature.trips.domain.viewmodel

internal fun normalizeDriverPhoneInput(input: String): String {
    val digits = input.filter { it in '0'..'9' }
    val local = if (input.trimStart().startsWith("+91") || (digits.length == 12 && digits.startsWith("91"))) {
        digits.drop(2)
    } else {
        digits
    }
    return local.take(10)
}

internal fun isValidDriverPhone(phone: String): Boolean = phone.matches(Regex("^[6-9][0-9]{9}$"))
