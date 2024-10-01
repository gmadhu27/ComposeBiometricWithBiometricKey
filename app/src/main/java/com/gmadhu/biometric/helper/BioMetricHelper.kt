package com.gmadhu.biometric.helper

import android.security.keystore.KeyGenParameterSpec.Builder
import android.security.keystore.KeyProperties
import android.util.Log
import com.gmadhu.biometric.helper.utils.ANDROID_KEYSTORE
import com.gmadhu.biometric.helper.utils.KEY_NAME
import java.security.KeyStore
import javax.crypto.KeyGenerator

object BioMetricHelper {

    // Function to create or get a key from the Keystore
    fun createBiometricKey() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)

            // If key already exists, no need to create it again
            if (!keyStore.containsAlias(KEY_NAME)) {
                val keyGenerator = KeyGenerator.getInstance("AES", ANDROID_KEYSTORE)
                keyGenerator.init(
                    Builder(
                        KEY_NAME,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setUserAuthenticationRequired(true) // Requires biometric authentication
                        .build()
                )
                keyGenerator.generateKey()
            }
        } catch (e: Exception) {
            Log.e("error", "Error generating key")
            e.printStackTrace()
        }
    }
}