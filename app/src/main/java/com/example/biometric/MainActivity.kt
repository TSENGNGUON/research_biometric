package com.example.biometric

import android.hardware.biometrics.BiometricManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.biometric.ui.theme.BiometricTheme

class MainActivity : FragmentActivity() {
    private lateinit var biometricManager: BiometricPromptManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Initialize the manager, passing the FragmentActivity instance (this)
        biometricManager = BiometricPromptManager(this)
        setContent {
            BiometricTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Host the main biometric composable
                    BiometricScreen(
                        manager = biometricManager,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BiometricTheme {
        Greeting("Android")
    }
}

@Composable
fun BiometricScreen(
    manager: BiometricPromptManager, modifier: Modifier
){
    // State to hold and display the authentication result message
    var authMessage by remember { mutableStateOf("Ready to authenticate.") }
    val context = LocalContext.current

    //Collect the authentication results Flow
    LaunchedEffect(manager) {
        manager.authResult.collect { result ->
            when (result) {
                BiometricAuthResult.Failed -> {
                    authMessage = "Authentication Failed. Try again."
                    Toast.makeText(context, authMessage, Toast.LENGTH_SHORT).show()
                }
                BiometricAuthResult.Success -> {
                    authMessage = "✅ Authentication Succeeded!"
                    Toast.makeText(context, authMessage, Toast.LENGTH_SHORT).show()
                }
                is BiometricAuthResult.Error -> {
                    authMessage = "Authentication Error (${result.errCode}): ${result.errorMessage}"
                    Toast.makeText(context, authMessage, Toast.LENGTH_LONG).show()
                }
                is BiometricAuthResult.NotAvailable -> {
                    authMessage = " Biometric Not Available: ${result.reason}"
                    Toast.makeText(context, authMessage, Toast.LENGTH_LONG).show()
                }

            }

        }



    }

    Column(
        modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Secure Login",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        //Trigger the showBiometricPrompt function on button click
        Button(
            onClick = {
                authMessage = "Biometric prompt showing..."
                manager.showBiometricPrompt(
                    title = "Access App",
                    description = "Use your fingerprint or face to verify your identity."
                )
            },
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
        ) {
            Text("Authenticate with Biometrics")
        }
        Spacer(modifier = Modifier.height(24.dp))


        // Display the current authentication status
        Text(
            text = authMessage,
            color = if(authMessage.startsWith("✅")) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )



    }

}