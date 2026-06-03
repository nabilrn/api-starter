package com.apistarter.common.security.encryption

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.security.encryption")
data class FieldEncryptionProperties(
    val enabled: Boolean = false,
    val key: String = "",
)
