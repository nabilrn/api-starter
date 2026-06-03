package com.apistarter.common.security.encryption

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.Base64

class EncryptedStringConverterSupportTest {
    private val key = Base64.getEncoder().encodeToString(ByteArray(32) { index -> index.toByte() })
    private val converter = TestEncryptedStringConverter(
        FieldEncryptionService(
            FieldEncryptionProperties(
                enabled = true,
                key = key,
            ),
        ),
    )

    @Test
    fun `converter encrypts database column and decrypts entity attribute`() {
        val databaseValue = converter.convertToDatabaseColumn("secret")
        val entityValue = converter.convertToEntityAttribute(databaseValue)

        assertThat(databaseValue).startsWith("enc:v1:")
        assertThat(entityValue).isEqualTo("secret")
    }

    @Test
    fun `converter keeps null values null`() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull()
        assertThat(converter.convertToEntityAttribute(null)).isNull()
    }
}

private class TestEncryptedStringConverter(
    fieldEncryptionService: FieldEncryptionService,
) : EncryptedStringConverterSupport(fieldEncryptionService)
