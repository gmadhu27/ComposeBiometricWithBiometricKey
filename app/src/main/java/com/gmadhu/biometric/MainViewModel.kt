package com.gmadhu.biometric

import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.biometric.BiometricManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gmadhu.biometric.helper.BiometricClass
import com.gmadhu.biometric.helper.BiometricClassDetails
import com.gmadhu.biometric.helper.BiometricProperties
import com.gmadhu.biometric.helper.BiometricType

class MainViewModel: ViewModel() {

    private val _biometricProperties = MutableLiveData<BiometricProperties>()
    val biometricProperties: LiveData<BiometricProperties> = _biometricProperties

    fun retrieveBiometricProperties(context: Context) {
        val biometricManager = BiometricManager.from(context)
        val keyGuardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        val availableBiometricTypes = getAvailableBiometricTypes(context.packageManager)
        val availableBiometricClasses = getAvailableBiometricClasses(biometricManager)

        _biometricProperties.value = BiometricProperties(
            isDeviceSecure = keyGuardManager.isDeviceSecure,
            availableBiometricTypes = availableBiometricTypes,
            availableBiometricClasses = availableBiometricClasses
        )
    }

    private fun getAvailableBiometricTypes(packageManager: PackageManager): List<BiometricType> {
        val types = mutableListOf<BiometricType>()
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_FACE)) types.add(BiometricType.FACE)
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_IRIS)) types.add(BiometricType.IRIS)
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) types.add(
            BiometricType.FINGERPRINT)
        return types
    }

    private fun getAvailableBiometricClasses(biometricManager: BiometricManager): List<BiometricClassDetails> {
        val classes = mutableListOf<BiometricClassDetails>()
        listOf(BiometricManager.Authenticators.BIOMETRIC_WEAK, BiometricManager.Authenticators.BIOMETRIC_STRONG).forEach { authenticator ->
            val result = biometricManager.canAuthenticate(authenticator)
            if (result == BiometricManager.BIOMETRIC_SUCCESS || result == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                classes.add(
                    BiometricClassDetails(
                    biometricClass = if (authenticator == BiometricManager.Authenticators.BIOMETRIC_WEAK) BiometricClass.CLASS2 else BiometricClass.CLASS3,
                    enrolled = result == BiometricManager.BIOMETRIC_SUCCESS
                )
                )
            }
        }
        return classes
    }
}
