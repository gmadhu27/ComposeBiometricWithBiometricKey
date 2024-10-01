package com.gmadhu.biometric.helper

import android.content.Context
import android.content.SharedPreferences

class BiometricPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("biometric_prefs", Context.MODE_PRIVATE)

    var isBiometricSet: Boolean
        get() = sharedPreferences.getBoolean("is_biometric_set", false)
        set(value) = sharedPreferences.edit().putBoolean("is_biometric_set", value).apply()
}
