package com.freight24.trucker.feature.profile.util

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
            // Validate Aadhaar checksum using Verhoeff algorithm (simplified check)
            !isValidAadhaarChecksum(cleanAadhaar) ->
                ValidationResult.Error("Invalid Aadhaar number")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validates Indian PAN Card number.
     * PAN is a 10-character alphanumeric unique identifier issued by Income Tax Department.
     * Format: 5 letters + 4 digits + 1 letter (uppercase)
     * 
     * @param pan The PAN number to validate
     * @return ValidationResult indicating success or error message
     */
    fun validatePan(pan: String): ValidationResult {
        if (pan.isEmpty()) {
            return ValidationResult.Success
        }
        
        val cleanPan = pan.uppercase().replace(" ", "").replace("-", "")
        
        return when {
            cleanPan.length != 10 -> 
                ValidationResult.Error("PAN must be 10 characters")
            !cleanPan.matches(Regex("^[A-Z]{5}[0-9]{4}[A-Z]{1}$")) -> 
                ValidationResult.Error("Invalid PAN format (e.g., ABCDE1234F)")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validates Indian GST (Goods and Services Tax) identification number.
     * GSTIN is a 15-character alphanumeric unique identifier.
     * Format: 2 digits (state code) + 10 characters (PAN) + 1 digit (entity number) + 1 character (Z) + 1 digit (checksum)
     * 
     * @param gst The GST number to validate
     * @return ValidationResult indicating success or error message
     */
    fun validateGst(gst: String): ValidationResult {
        if (gst.isEmpty()) {
            return ValidationResult.Success
        }
        
        val cleanGst = gst.uppercase().replace(" ", "").replace("-", "")
        
        return when {
            cleanGst.length != 15 -> 
                ValidationResult.Error("GST must be 15 characters")
            !cleanGst.matches(Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9]{1}$")) -> 
                ValidationResult.Error("Invalid GST format (e.g., 27ABCDE1234F1Z5)")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validates Indian IFSC (Indian Financial System Code) code.
     * IFSC is an 11-character alphanumeric code for bank branch identification.
     * Format: 4 letters (bank code) + 0 (reserved character) + 6 characters (branch code)
     * 
     * @param ifsc The IFSC code to validate
     * @return ValidationResult indicating success or error message
     */
    fun validateIfsc(ifsc: String): ValidationResult {
        if (ifsc.isEmpty()) {
            return ValidationResult.Success
        }
        
        val cleanIfsc = ifsc.uppercase().replace(" ", "").replace("-", "")
        
        return when {
            cleanIfsc.length != 11 -> 
                ValidationResult.Error("IFSC must be 11 characters")
            !cleanIfsc.matches(Regex("^[A-Z]{4}0[A-Z]{6}$")) -> 
                ValidationResult.Error("Invalid IFSC format (e.g., HDFC0001234)")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validates UPI (Unified Payments Interface) ID.
     * UPI ID format: username@bankname or username@upi
     * 
     * @param upiId The UPI ID to validate
     * @return ValidationResult indicating success or error message
     */
    fun validateUpiId(upiId: String): ValidationResult {
        if (upiId.isEmpty()) {
            return ValidationResult.Success
        }
        
        val cleanUpi = upiId.lowercase().trim()
        
        return when {
            cleanUpi.length < 8 || cleanUpi.length > 50 ->
                ValidationResult.Error("UPI ID must be 8-50 characters")
            !cleanUpi.matches(Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+$")) ->
                ValidationResult.Error("Invalid UPI format (e.g., name@okhdfcbank)")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validates Indian phone number (mobile).
     * Format: +91 followed by 10 digits, or just 10 digits starting with 6-9
     * 
     * @param phone The phone number to validate
     * @return ValidationResult indicating success or error message
     */
    fun validateIndianPhone(phone: String): ValidationResult {
        if (phone.isEmpty()) {
            return ValidationResult.Success
        }
        
        val cleanPhone = phone.replace(" ", "").replace("-", "").replace("+91", "")
        
        return when {
            cleanPhone.length != 10 ->
                ValidationResult.Error("Phone must be 10 digits")
            !cleanPhone.all { it.isDigit() } ->
                ValidationResult.Error("Phone must contain only digits")
            !cleanPhone.matches(Regex("^[6-9][0-9]{9}$")) ->
                ValidationResult.Error("Phone must start with 6, 7, 8, or 9")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validates email address using standard pattern.
     * 
     * @param email The email address to validate
     * @return ValidationResult indicating success or error message
     */
    fun validateEmail(email: String): ValidationResult {
        if (email.isEmpty()) {
            return ValidationResult.Success
        }
        
        return when {
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                ValidationResult.Error("Invalid email format")
            email.length > 254 ->
                ValidationResult.Error("Email is too long")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validates bank account number.
     * Indian bank account numbers are typically 9-18 digits.
     * 
     * @param accountNumber The account number to validate
     * @return ValidationResult indicating success or error message
     */
    fun validateAccountNumber(accountNumber: String): ValidationResult {
        if (accountNumber.isEmpty()) {
            return ValidationResult.Success
        }
        
        val cleanAccount = accountNumber.replace(" ", "").replace("-", "")
        
        return when {
            cleanAccount.length < 9 ->
                ValidationResult.Error("Account number must be at least 9 digits")
            cleanAccount.length > 18 ->
                ValidationResult.Error("Account number must be at most 18 digits")
            !cleanAccount.all { it.isDigit() } ->
                ValidationResult.Error("Account number must contain only digits")
            else -> ValidationResult.Success
        }
    }

    /**
     * Simplified Aadhaar checksum validation.
     * Note: Full validation requires Verhoeff algorithm implementation.
     * This is a basic structural validation.
     */
    private fun isValidAadhaarChecksum(aadhaar: String): Boolean {
        // Basic check: first digit should not be 0
        // Real validation would require Verhoeff algorithm
        if (aadhaar.length != 12) return false
        if (aadhaar.startsWith("0")) return false
        
        // All digits
        if (!aadhaar.all { it.isDigit() }) return false
        
        // Check digit can be validated with Verhoeff algorithm
        // For now, accept if format is correct and passes basic check
        return true
    }

    /**
     * Formats Aadhaar number with space after every 4 digits.
     * Input: 123456789012 -> Output: 1234 5678 9012
     */
    fun formatAadhaar(aadhaar: String): String {
        val clean = aadhaar.replace(" ", "").replace("-", "").take(12)
        return clean.chunked(4).joinToString(" ")
    }

    /**
     * Formats phone number with space after every 5 digits.
     * Input: 9876543210 -> Output: 98765 43210
     */
    fun formatPhone(phone: String): String {
        val clean = phone.replace(" ", "").replace("-", "").replace("+91", "").take(10)
        return clean.chunked(5).joinToString(" ").trim()
    }
}

/**
 * Sealed class representing validation results.
 */
sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
