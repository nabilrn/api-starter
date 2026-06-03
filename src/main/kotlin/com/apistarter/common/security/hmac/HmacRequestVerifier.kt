package com.apistarter.common.security.hmac

import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

@Service
class HmacRequestVerifier(
    private val properties: HmacProperties,
    private val signatureService: HmacSignatureService,
    private val nonceStore: ReplayNonceStore,
) {
    fun verifySignature(
        request: SignedRequest,
        signature: String,
    ): HmacVerificationResult {
        if (!properties.enabled) return HmacVerificationResult.Valid
        if (properties.secret.isBlank()) return HmacVerificationResult.Invalid("HMAC secret is not configured")
        if (signature.isBlank()) return HmacVerificationResult.Invalid("Missing HMAC signature")
        if (request.timestamp.isBlank()) return HmacVerificationResult.Invalid("Missing HMAC timestamp")
        if (request.nonce.isBlank()) return HmacVerificationResult.Invalid("Missing HMAC nonce")

        val timestamp = runCatching { Instant.parse(request.timestamp) }.getOrNull()
            ?: return HmacVerificationResult.Invalid("Invalid HMAC timestamp")
        val now = Instant.now()
        val skew = Duration.between(timestamp, now).abs().seconds
        if (skew > properties.maxSkewSeconds) {
            return HmacVerificationResult.Invalid("HMAC timestamp is outside the allowed window")
        }

        if (!signatureService.matches(request, signature)) {
            return HmacVerificationResult.Invalid("Invalid HMAC signature")
        }

        val nonceExpiresAt = now.plusSeconds(properties.maxSkewSeconds)
        if (!nonceStore.remember(request.nonce, nonceExpiresAt)) {
            return HmacVerificationResult.Invalid("HMAC nonce has already been used")
        }

        return HmacVerificationResult.Valid
    }
}

sealed class HmacVerificationResult {
    data object Valid : HmacVerificationResult()

    data class Invalid(
        val reason: String,
    ) : HmacVerificationResult()
}
