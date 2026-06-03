package com.apistarter.common.security.hmac

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class HmacRequestVerifierTest {
    private val properties = HmacProperties(
        enabled = true,
        secret = "hmac-secret-minimum-32-characters",
        maxSkewSeconds = 300,
    )
    private val signatureService = HmacSignatureService(properties)
    private val nonceStore = InMemoryReplayNonceStore()
    private val verifier = HmacRequestVerifier(properties, signatureService, nonceStore)

    @Test
    fun `valid request is accepted`() {
        val request = signedRequest()
        val signature = signatureService.sign(request)

        assertThat(verifier.verifySignature(request, signature)).isEqualTo(HmacVerificationResult.Valid)
    }

    @Test
    fun `reused nonce is rejected`() {
        val request = signedRequest(nonce = "same-nonce")
        val signature = signatureService.sign(request)

        assertThat(verifier.verifySignature(request, signature)).isEqualTo(HmacVerificationResult.Valid)
        val secondResult = verifier.verifySignature(request, signature)

        assertThat(secondResult).isInstanceOf(HmacVerificationResult.Invalid::class.java)
        assertThat((secondResult as HmacVerificationResult.Invalid).reason).contains("already been used")
    }

    @Test
    fun `old timestamp is rejected`() {
        val request = signedRequest(
            timestamp = Instant.now().minus(10, ChronoUnit.MINUTES).toString(),
        )
        val signature = signatureService.sign(request)

        val result = verifier.verifySignature(request, signature)

        assertThat(result).isInstanceOf(HmacVerificationResult.Invalid::class.java)
        assertThat((result as HmacVerificationResult.Invalid).reason).contains("outside the allowed window")
    }

    @Test
    fun `disabled hmac accepts request`() {
        val disabledVerifier = HmacRequestVerifier(
            properties = properties.copy(enabled = false),
            signatureService = signatureService,
            nonceStore = nonceStore,
        )

        assertThat(disabledVerifier.verifySignature(signedRequest(), "")).isEqualTo(HmacVerificationResult.Valid)
    }

    private fun signedRequest(
        timestamp: String = Instant.now().toString(),
        nonce: String = "nonce-${System.nanoTime()}",
    ): SignedRequest = SignedRequest(
        method = "POST",
        pathAndQuery = "/api/v1/webhooks/payment",
        timestamp = timestamp,
        nonce = nonce,
        body = """{"status":"PAID"}""",
    )
}
