package com.apistarter.common.security.encryption

import com.apistarter.common.error.BadRequestException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.util.Base64

class FieldEncryptionServiceTest {
    private val key = Base64.getEncoder().encodeToString(ByteArray(32) { index -> index.toByte() })

    @Test
    fun `encrypt and decrypt round trip sensitive string`() {
        val service = service()

        val encrypted = service.encrypt("secret-api-key")
        val decrypted = service.decrypt(encrypted)

        assertThat(encrypted).startsWith("enc:v1:")
        assertThat(encrypted).isNotEqualTo("secret-api-key")
        assertThat(decrypted).isEqualTo("secret-api-key")
    }

    @Test
    fun `encrypt uses random iv so same plaintext produces different values`() {
        val service = service()

        val first = service.encrypt("same-secret")
        val second = service.encrypt("same-secret")

        assertThat(first).isNotEqualTo(second)
        assertThat(service.decrypt(first)).isEqualTo("same-secret")
        assertThat(service.decrypt(second)).isEqualTo("same-secret")
    }

    @Test
    fun `disabled service returns values unchanged`() {
        val service = service(enabled = false)

        assertThat(service.encrypt("plain")).isEqualTo("plain")
        assertThat(service.decrypt("plain")).isEqualTo("plain")
    }

    @Test
    fun `blank value stays blank`() {
        val service = service()

        assertThat(service.encrypt("")).isEmpty()
        assertThat(service.decrypt("")).isEmpty()
    }

    @Test
    fun `decrypt rejects plaintext when encryption enabled`() {
        val service = service()

        assertThatThrownBy { service.decrypt("plain") }
            .isInstanceOf(BadRequestException::class.java)
            .hasMessageContaining("invalid format")
    }

    @Test
    fun `decrypt rejects tampered ciphertext`() {
        val service = service()
        val encrypted = service.encrypt("secret")
        val tampered = encrypted.dropLast(2) + "xx"

        assertThatThrownBy { service.decrypt(tampered) }
            .isInstanceOf(BadRequestException::class.java)
            .hasMessageContaining("could not be decrypted")
    }

    @Test
    fun `missing key is rejected`() {
        val service = FieldEncryptionService(
            FieldEncryptionProperties(
                enabled = true,
                key = "",
            ),
        )

        assertThatThrownBy { service.encrypt("secret") }
            .isInstanceOf(BadRequestException::class.java)
            .hasMessageContaining("key is not configured")
    }

    @Test
    fun `wrong key size is rejected`() {
        val shortKey = Base64.getEncoder().encodeToString(ByteArray(16))
        val service = FieldEncryptionService(
            FieldEncryptionProperties(
                enabled = true,
                key = shortKey,
            ),
        )

        assertThatThrownBy { service.encrypt("secret") }
            .isInstanceOf(BadRequestException::class.java)
            .hasMessageContaining("32 bytes")
    }

    @Test
    fun `invalid base64 key is rejected`() {
        val service = FieldEncryptionService(
            FieldEncryptionProperties(
                enabled = true,
                key = "not base64",
            ),
        )

        assertThatThrownBy { service.encrypt("secret") }
            .isInstanceOf(BadRequestException::class.java)
            .hasMessageContaining("base64")
    }

    private fun service(enabled: Boolean = true): FieldEncryptionService =
        FieldEncryptionService(
            FieldEncryptionProperties(
                enabled = enabled,
                key = key,
            ),
        )
}
