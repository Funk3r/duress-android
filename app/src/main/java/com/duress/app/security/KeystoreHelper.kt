package com.duress.app.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object KeystoreHelper {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val MASTER_KEY_ALIAS = "duress_sec_master_key_v1"
    private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12

    private fun getOrCreateMasterKey(): SecretKey? {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (keyStore.containsAlias(MASTER_KEY_ALIAS)) {
                val entry = keyStore.getEntry(MASTER_KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
                return entry?.secretKey
            }

            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val spec = KeyGenParameterSpec.Builder(
                MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(spec)
            keyGenerator.generateKey()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Encrypts plaintext string using AES-256-GCM backed by hardware KeyStore.
     * Returns Base64-encoded string: (IV + Ciphertext).
     */
    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return ""
        return try {
            val secretKey = getOrCreateMasterKey()
            if (secretKey != null) {
                val cipher = Cipher.getInstance(AES_TRANSFORMATION)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                val iv = cipher.iv
                val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
                val combined = ByteArray(iv.size + cipherBytes.size)
                System.arraycopy(iv, 0, combined, 0, iv.size)
                System.arraycopy(cipherBytes, 0, combined, iv.size, cipherBytes.size)
                Base64.encodeToString(combined, Base64.NO_WRAP)
            } else {
                // Fallback for devices with broken KeyStore
                softwareFallbackObfuscate(plainText)
            }
        } catch (e: Exception) {
            softwareFallbackObfuscate(plainText)
        }
    }

    /**
     * Decrypts Base64-encoded string (IV + Ciphertext) using KeyStore key.
     */
    fun decrypt(encryptedBase64: String): String? {
        if (encryptedBase64.isEmpty()) return null
        return try {
            if (encryptedBase64.startsWith("FB:")) {
                return softwareFallbackDeobfuscate(encryptedBase64)
            }

            val secretKey = getOrCreateMasterKey()
            if (secretKey != null) {
                val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
                if (combined.size <= GCM_IV_LENGTH) return null

                val iv = ByteArray(GCM_IV_LENGTH)
                val cipherBytes = ByteArray(combined.size - GCM_IV_LENGTH)
                System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH)
                System.arraycopy(combined, GCM_IV_LENGTH, cipherBytes, 0, cipherBytes.size)

                val cipher = Cipher.getInstance(AES_TRANSFORMATION)
                val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
                val plainBytes = cipher.doFinal(cipherBytes)
                String(plainBytes, Charsets.UTF_8)
            } else {
                softwareFallbackDeobfuscate(encryptedBase64)
            }
        } catch (e: Exception) {
            softwareFallbackDeobfuscate(encryptedBase64)
        }
    }

    /**
     * Hashes PIN with salt and KeyStore encryption for secure comparison.
     */
    fun hashAndEncryptPin(pin: String): String {
        return try {
            val salt = "duress_secure_salt_pakistan_v1"
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest((salt + pin).toByteArray(Charsets.UTF_8))
            val hashString = hashBytes.joinToString("") { "%02x".format(it) }
            encrypt(hashString)
        } catch (e: Exception) {
            softwareFallbackObfuscate(pin)
        }
    }

    /**
     * Verifies if input PIN matches stored encrypted hash.
     */
    fun verifyPin(inputPin: String, storedEncryptedHash: String): Boolean {
        if (storedEncryptedHash.isBlank()) return false
        return try {
            val decryptedHash = decrypt(storedEncryptedHash) ?: return false
            val salt = "duress_secure_salt_pakistan_v1"
            val digest = MessageDigest.getInstance("SHA-256")
            val inputHashBytes = digest.digest((salt + inputPin).toByteArray(Charsets.UTF_8))
            val inputHashString = inputHashBytes.joinToString("") { "%02x".format(it) }
            inputHashString == decryptedHash || decryptedHash == inputPin
        } catch (e: Exception) {
            false
        }
    }

    private fun softwareFallbackObfuscate(text: String): String {
        val encoded = Base64.encodeToString(text.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        return "FB:$encoded"
    }

    private fun softwareFallbackDeobfuscate(text: String): String? {
        return try {
            if (text.startsWith("FB:")) {
                val raw = text.removePrefix("FB:")
                String(Base64.decode(raw, Base64.NO_WRAP), Charsets.UTF_8)
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
