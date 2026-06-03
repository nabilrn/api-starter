package com.apistarter.common.security.encryption

import com.apistarter.common.error.BadRequestException
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Service
class FieldEncryptionService(
    private val properties: FieldEncryptionProperties,
) {
    private val secureRandom = SecureRandom()
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()

    fun encrypt(plainText: String): String {
        if (!properties.enabled) return plainText
        if (plainText.isEmpty()) return plainText

        val iv = ByteArray(GCM_IV_BYTES)
        secureRandom.nextBytes(iv)

        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        return listOf(
            ENCRYPTED_PREFIX,
            encoder.encodeToString(iv),
            encoder.encodeToString(cipherText),
        ).joinToString(":")
    }

    fun decrypt(value: String): String {
        if (!properties.enabled) return value
        if (value.isEmpty()) return value
        if (!isEncrypted(value)) {
            throw BadRequestException("Encrypted value has an invalid format")
        }

        val parts = value.split(":")
        if (parts.size != 4) {
            throw BadRequestException("Encrypted value has an invalid format")
        }

        val iv = decodePart(parts[2])
        val cipherText = decodePart(parts[3])
        if (iv.size != GCM_IV_BYTES) {
            throw BadRequestException("Encrypted value has an invalid initialization vector")
        }

        return runCatching {
            val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
            cipher.doFinal(cipherText).toString(Charsets.UTF_8)
        }.getOrElse {
            throw BadRequestException("Encrypted value could not be decrypted")
        }
    }

    fun isEncrypted(value: String): Boolean = value.startsWith("$ENCRYPTED_PREFIX:")

    private fun secretKey(): SecretKeySpec {
        if (properties.key.isBlank()) {
            throw BadRequestException("Field encryption key is not configured")
        }

        val keyBytes = runCatching { Base64.getDecoder().decode(properties.key) }
            .getOrElse { throw BadRequestException("Field encryption key must be base64 encoded") }

        if (keyBytes.size != AES_256_KEY_BYTES) {
            throw BadRequestException("Field encryption key must be 32 bytes")
        }

        return SecretKeySpec(keyBytes, AES_ALGORITHM)
    }

    private fun decodePart(value: String): ByteArray =
        runCatching { decoder.decode(value) }
            .getOrElse { throw BadRequestException("Encrypted value has invalid base64 data") }

    companion object {
        const val ENCRYPTED_PREFIX = "enc:v1"
        private const val AES_ALGORITHM = "AES"
        private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val AES_256_KEY_BYTES = 32
        private const val GCM_IV_BYTES = 12
        private const val GCM_TAG_BITS = 128
    }
}
