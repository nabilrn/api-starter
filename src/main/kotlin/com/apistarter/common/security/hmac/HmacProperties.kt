package com.apistarter.common.security.hmac

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.security.hmac")
data class HmacProperties(
    val enabled: Boolean = false,
    val secret: String = "",
    val maxSkewSeconds: Long = 300,
    val protectedPaths: List<String> = listOf("/api/v1/webhooks", "/api/v1/internal"),
)
