package com.example.biometric

import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.AnimationEndReason
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricPrompt.PromptInfo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class BiometricPromptManager(
    private val activity: FragmentActivity
) {
    private val TAG = "BiometricPromptManager"


    // Create channel to send result
    private val resultChannel = Channel<BiometricAuthResult>()

    // Expose the channel as a Flow for UI to Collect
    val authResult = resultChannel.receiveAsFlow()
    interface BiometricAuthListener {
        fun onAuthenticationSucceeded()
        fun onAuthenticationFailed()
        fun onAuthenticationError(errorCode: Int, errString: CharSequence)
        fun onBiometricNotAvailable(reason: String)
    }


    var listener: BiometricAuthListener = object : BiometricAuthListener {
        override fun onAuthenticationSucceeded() {

        }

        override fun onAuthenticationFailed() {

        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {

        }

        override fun onBiometricNotAvailable(reason: String) {

        }

    }

    fun showBiometricPrompt(title: String, description: String){
        val biometricManager = BiometricManager.from(activity)

        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)){
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(TAG, "App can authenticate using biometrics.")
                // Biometrics are available, proceed to show the prompt
                val promptInfo = createPromptInfo(title, description)
                val biometricPrompt = createBiometricPrompt()
                biometricPrompt.authenticate(promptInfo)
            }


            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE-> {
                val message = "Biometric hardware is unavailable."
                Log.e(TAG, message)
                listener.onBiometricNotAvailable(message)
                resultChannel.trySend(BiometricAuthResult.NotAvailable(message))
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                val message = "The user has not enrolled any biometrics."
                Log.e(TAG, message)
                listener.onBiometricNotAvailable(message)
                resultChannel.trySend(BiometricAuthResult.NotAvailable(message))
            }

            else -> {
                val message = "Biometric check failed with unknow error."
                Log.e(TAG, message)
                listener.onBiometricNotAvailable(message)
                resultChannel.trySend(BiometricAuthResult.NotAvailable(message))
            }
        }
    }

    private fun createPromptInfo(title: String, description: String): PromptInfo {
            return PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle("Confirm your indentity")
                .setDescription(description)
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .setNegativeButtonText("Cancel")
                .build()
    }
    /**
     * Creates the BiometricPrompt instance with the required callbacks.
     */
    private fun createBiometricPrompt(): BiometricPrompt {
        val executor = activity.mainExecutor

        val callback  = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.e(TAG, "Auth error ($errorCode): $errString")
                listener.onAuthenticationError(errorCode, errString)
                resultChannel.trySend(BiometricAuthResult.Error(errorCode, errString))
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d(TAG, "Auth succeeded!")
                listener.onAuthenticationSucceeded()
                resultChannel.trySend(BiometricAuthResult.Success)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.d(TAG, "Auth Failed!")
                listener.onAuthenticationFailed()
                resultChannel.trySend(BiometricAuthResult.Failed)
            }


        }
        // BiometricPrompt requires a FragmentActivity (or AppCompatActivity) and an AuthenticationCallback
        return BiometricPrompt(activity, executor, callback)
    }
}