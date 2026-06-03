package com.apistarter.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.security.jwt")
data class JwtProperties(
    val accessSecret: String,
    val refreshSecret: String,
    val accessTtlMinutes: Long,
    val refreshTtlDays: Long,
)
