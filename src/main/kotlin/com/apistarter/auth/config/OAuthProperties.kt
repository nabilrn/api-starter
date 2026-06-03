package com.apistarter.auth.config

import com.apistarter.auth.domain.OAuthProvider
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.oauth")
data class OAuthProperties(
    val google: OAuthProviderProperties = OAuthProviderProperties(),
    val github: OAuthProviderProperties = OAuthProviderProperties(),
) {
    fun isEnabled(provider: OAuthProvider): Boolean = when (provider) {
        OAuthProvider.GOOGLE -> google.enabled
        OAuthProvider.GITHUB -> github.enabled
    }
}

data class OAuthProviderProperties(
    val enabled: Boolean = false,
    val clientId: String = "",
    val clientSecret: String = "",
)
