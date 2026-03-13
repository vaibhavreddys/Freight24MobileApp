package com.freight24.connect.feature.truckowners.profile.util

import android.util.Patterns

/**
 * Validation utilities for Indian-specific identity and financial fields.
 * Provides regex-based validation following Indian government and banking standards.
 */
object ProfileFieldValidators {

    /**
     * Validates Indian Aadhaar Card number.
     * Aadhaar is a 12-digit unique number issued by UIDAI.
     * Format: 12 digits (no spaces/dashes)
     * 
     * @param aadhaar The Aadhaar number to validate
     * @return ValidationResult indicating success or error message
     */
    fun validateAadhaar(aadhaar: String): ValidationResult {
        if (aadhaar.isEmpty()) {
            return ValidationResult.Success
        }
        
        val cleanAadhaar = aadhaar.replace(" ", "").replace("-", "")
        
        return when {
            cleanAadhaar.length != 12 -> 
                ValidationResult.Error("Aadhaar must be 12 digits")
            !cleanAadhaar.all { it.isDigit() } -> 
                ValidationResult.Error("Aadhaar must contain only digits")
            cleanAadhaar.startsWith("0") || cleanAadhaar.startsWith("1") ->
                ValidationResult.Error("Aadhaar cannot start with 0 or 1")
            !validateVerhoeff(cleanAadhaar) ->
                ValidationResult.Error("Invalid Aadhaar checksum")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validates Indian PAN Card number.
     * Format: 5 letters + 4 digits + 1 letter
     */
    fun validatePan(pan: String): ValidationResult {
        if (pan.isEmpty()) {
            return ValidationResult.Success
        }
        
        val cleanPan = pan.uppercase().trim()
        
        return when {
            cleanPan.length != 10 ->
                ValidationResult.Error("PAN must be 10 characters")
            !cleanPan.matches(Regex("^[A-Z]{5}[0-9]{4}[A-Z]$")) ->
                ValidationResult.Error("Invalid PAN format")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validates Indian GST number.
     * Format: 2 digits (state) + PAN (10) + 1 char (entity) + Z + 1 char (checksum)
     */
    fun validateGst(gst: String): ValidationResult {
        if (gst.isEmpty()) {
            return ValidationResult.Success
        }
        
        val cleanGst = gst.uppercase().trim()
        
        return when {
            cleanGst.length != 15 -> 
                ValidationResult.Error("GST must be 15 characters")
            !cleanGst.matches(Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$")) -> 
                ValidationResult.Error("Invalid GST format")
            // The 3rd to 12th characters of GST are the PAN
            validatePan(cleanGst.substring(2, 12)) is ValidationResult.Error ->
                ValidationResult.Error("Invalid PAN component in GST")
            else -> ValidationResult.Success
        }
    }

    /**
     * Verhoeff Algorithm for Aadhaar validation
     */
    private fun validateVerhoeff(num: String): Boolean {
        // 1. Guard clause: Ensure it's exactly 12 digits to prevent crashes
        if (!num.matches(Regex("^[0-9]{12}$"))) {
            return false
        }

        // 2. The multiplication table
        val d = arrayOf(
            intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
            intArrayOf(1, 2, 3, 4, 0, 6, 7, 8, 9, 5),
            intArrayOf(2, 3, 4, 0, 1, 7, 8, 9, 5, 6),
            intArrayOf(3, 4, 0, 1, 2, 8, 9, 5, 6, 7),
            intArrayOf(4, 0, 1, 2, 3, 9, 5, 6, 7, 8),
            intArrayOf(5, 9, 8, 7, 6, 0, 4, 3, 2, 1),
            intArrayOf(6, 5, 9, 8, 7, 1, 0, 4, 3, 2),
            intArrayOf(7, 6, 5, 9, 8, 2, 1, 0, 4, 3),
            intArrayOf(8, 7, 6, 5, 9, 3, 2, 1, 0, 4),
            intArrayOf(9, 8, 7, 6, 5, 4, 3, 2, 1, 0)
        )

        // 3. The permutation table (with the fixed last row)
        val p = arrayOf(
            intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
            intArrayOf(1, 5, 7, 6, 2, 8, 3, 0, 9, 4),
            intArrayOf(5, 8, 0, 3, 7, 9, 6, 1, 4, 2),
            intArrayOf(8, 9, 1, 6, 0, 4, 3, 5, 2, 7),
            intArrayOf(9, 4, 5, 3, 1, 2, 6, 8, 7, 0),
            intArrayOf(4, 2, 8, 6, 5, 7, 3, 9, 0, 1),
            intArrayOf(2, 7, 9, 3, 8, 0, 6, 4, 1, 5),
            intArrayOf(7, 0, 4, 6, 9, 1, 3, 2, 5, 8)
        )

        // Note: The inverse table is used for generating the checksum, not validating it.
        // val inv = intArrayOf(0, 4, 3, 2, 1, 5, 6, 7, 8, 9)

        var c = 0
        val reversed = num.reversed().toCharArray()

        for (i in reversed.indices) {
            c = d[c][p[i % 8][reversed[i] - '0']]
        }

        return c == 0
    }

    /**
     * Validates Indian IFSC code.
     */
    fun validateIfsc(ifsc: String): ValidationResult {
        if (ifsc.isEmpty()) {
            return ValidationResult.Success
        }
        
        val cleanIfsc = ifsc.uppercase().trim()
        
        return when {
            cleanIfsc.length != 11 -> 
                ValidationResult.Error("IFSC must be 11 characters")
            !cleanIfsc.matches(Regex("^[A-Z]{4}0[A-Z0-9]{6}$")) -> 
                ValidationResult.Error("Invalid IFSC format")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validates UPI ID.
     */
    fun validateUpiId(upiId: String): ValidationResult {
        if (upiId.isEmpty()) {
            return ValidationResult.Success
        }
        
        val cleanUpi = upiId.lowercase().trim()
        
        return when {
            !cleanUpi.contains("@") ->
                ValidationResult.Error("UPI ID must contain @")
            !cleanUpi.matches(Regex("^[a-zA-Z0-9._-]+@[a-zA-Z]{2,}$")) ->
                ValidationResult.Error("Invalid UPI format")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validates Indian phone number.
     */
    fun validateIndianPhone(phone: String): ValidationResult {
        if (phone.isEmpty()) {
            return ValidationResult.Success
        }
        
        val cleanPhone = phone.replace(" ", "").replace("-", "").replace("+91", "")
        
        return when {
            cleanPhone.length != 10 ->
                ValidationResult.Error("Phone must be 10 digits")
            !cleanPhone.matches(Regex("^[6-9][0-9]{9}$")) ->
                ValidationResult.Error("Must start with 6-9")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validates email address.
     */
    fun validateEmail(email: String): ValidationResult {
        if (email.isEmpty()) {
            return ValidationResult.Success
        }
        
        return when {
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                ValidationResult.Error("Invalid email format")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validates bank account number.
     */
    fun validateAccountNumber(accountNumber: String): ValidationResult {
        if (accountNumber.isEmpty()) {
            return ValidationResult.Success
        }
        
        val cleanAccount = accountNumber.replace(" ", "")
        
        return when {
            cleanAccount.length !in 9..18 ->
                ValidationResult.Error("Account must be 9-18 digits")
            !cleanAccount.all { it.isDigit() } ->
                ValidationResult.Error("Digits only")
            else -> ValidationResult.Success
        }
    }

    /**
     * Formats Aadhaar number: XXXX XXXX XXXX
     */
    fun formatAadhaar(aadhaar: String): String {
        val clean = aadhaar.replace(" ", "").take(12)
        return clean.chunked(4).joinToString(" ")
    }

    /**
     * Formats phone number: XXXXX XXXXX
     */
    fun formatPhone(phone: String): String {
        val clean = phone.replace(" ", "").replace("+91", "").take(10)
        return clean.chunked(5).joinToString(" ").trim()
    }
}

sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
