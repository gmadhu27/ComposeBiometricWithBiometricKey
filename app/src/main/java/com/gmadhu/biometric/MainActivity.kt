package com.gmadhu.biometric

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.gmadhu.biometric.helper.BioMetricHelper
import com.gmadhu.biometric.helper.BioMetricHelper.getCipher
import com.gmadhu.biometric.helper.BiometricClassDetails
import com.gmadhu.biometric.helper.BiometricPreferences
import com.gmadhu.biometric.helper.BiometricProperties
import com.gmadhu.biometric.helper.utils.ANDROID_KEYSTORE
import com.gmadhu.biometric.helper.utils.KEY_NAME
import com.gmadhu.biometric.ui.theme.BiometricTheme

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BiometricTheme {
                BiometricClassScreen(viewModel) {
                    val preferences = BiometricPreferences(this)
                    if (preferences.isBiometricSet) {
                        val cipher = getCipher(KEY_NAME, ANDROID_KEYSTORE, this)
                        if (cipher == null) {
                            preferences.isBiometricSet = false
                            showBiometricPrompt(
                                "Device biometric changes, update biometric",
                                "Set up biometric credential"
                            )
                        } else {
                            showBiometricPrompt(
                                "Biometric login for your app",
                                "Log in using your biometric credential"
                            )
                        }
                    } else {
                        showBiometricPrompt(
                            "Biometric setup for your app", "Set up biometric"
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.retrieveBiometricProperties(this)
    }


    private fun showBiometricPrompt(title: String, subTitle: String) {

        val biometricPrompt = BiometricPrompt(this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    showToast("Authentication error: $errString")
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    BioMetricHelper.createBiometricKey(KEY_NAME, ANDROID_KEYSTORE)
                    val preferences = BiometricPreferences(this@MainActivity)
                    preferences.isBiometricSet = true
                    showToast("Authentication succeeded!")
                }

                override fun onAuthenticationFailed() {
                    showToast("Authentication failed")
                }
            })

        val promptInfo =
            BiometricPrompt.PromptInfo.Builder().setTitle(title).setSubtitle(subTitle)
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
                .setNegativeButtonText("Cancel").build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun BiometricClassScreen(viewModel: MainViewModel, onShowBiometricPromptClick: () -> Unit) {
    val biometricPropertiesState = viewModel.biometricProperties.observeAsState()

    BiometricClassDisplayScreen(
        state = biometricPropertiesState.value,
        onShowBiometricPromptClick = onShowBiometricPromptClick,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun BiometricClassDisplayScreen(
    state: BiometricProperties?,
    onShowBiometricPromptClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        state?.let {
            Column(
                modifier = modifier.padding(16.dp)
            ) {
                DeviceSecureDisplay(
                    isDeviceSecure = state.isDeviceSecure,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                BiometricTypesDisplay(
                    biometricTypes = state.availableBiometricTypes.joinToString(separator = ", "),
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                BiometricClassesDisplay(
                    biometricClasses = state.availableBiometricClasses,
                )
                Button(
                    onClick = onShowBiometricPromptClick,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 24.dp)
                ) {
                    Text("Show Biometric Prompt")
                }
            }
        }
    }
}

@Composable
fun DeviceSecureDisplay(isDeviceSecure: Boolean, modifier: Modifier = Modifier) {
    Text(
        text = "Device is protected with a PIN, pattern or password: $isDeviceSecure",
        modifier = modifier
    )
}

@Composable
fun BiometricTypesDisplay(biometricTypes: String, modifier: Modifier = Modifier) {
    Text(
        text = "Available biometric types: $biometricTypes", modifier = modifier
    )
}

@Composable
fun BiometricClassesDisplay(
    biometricClasses: List<BiometricClassDetails>, modifier: Modifier = Modifier
) {
    Column {
        Text(
            text = "Available biometric classes:",
            modifier = Modifier.padding(bottom = 8.dp),
        )
        biometricClasses.forEach {
            Text(
                text = "${it.biometricClass}, enrolled: ${it.enrolled}",
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
    }
}
