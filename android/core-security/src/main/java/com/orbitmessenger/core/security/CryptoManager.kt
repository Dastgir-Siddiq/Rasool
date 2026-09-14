package com.orbitmessenger.core.security

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class CryptoManager(private val keyStoreManager: KeyStoreManager) {

    companion object {
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val TAG_LENGTH_BIT = 128
        private const val ALIAS = "orbit_master_key"
    }

    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val secretKey = keyStoreManager.getOrGenerateSecretKey(ALIAS)
        
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        
        // Format: IV:EncryptedData
        val ivString = Base64.encodeToString(iv, Base64.NO_WRAP)
        val dataString = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        
        return "$ivString:$dataString"
    }

    fun decrypt(encryptedText: String): String {
        val parts = encryptedText.split(":")
        if (parts.size != 2) return encryptedText // Probably not encrypted

        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val encryptedBytes = Base64.decode(parts[1], Base64.NO_WRAP)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val secretKey = keyStoreManager.getOrGenerateSecretKey(ALIAS)
        val spec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        
        return String(decryptedBytes, Charsets.UTF_8)
    }
}
