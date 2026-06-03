package com.apistarter.common.security.encryption

import jakarta.persistence.AttributeConverter

abstract class EncryptedStringConverterSupport(
    private val fieldEncryptionService: FieldEncryptionService,
) : AttributeConverter<String, String> {
    override fun convertToDatabaseColumn(attribute: String?): String? =
        attribute?.let { fieldEncryptionService.encrypt(it) }

    override fun convertToEntityAttribute(dbData: String?): String? =
        dbData?.let { fieldEncryptionService.decrypt(it) }
}
