package com.example.biometric

sealed class BiometricAuthResult {
    data object Success: BiometricAuthResult()
    data object Failed : BiometricAuthResult()
    data class Error(val errCode: Int, val errorMessage: CharSequence) : BiometricAuthResult()
    data class NotAvailable(val reason: String) : BiometricAuthResult()
}