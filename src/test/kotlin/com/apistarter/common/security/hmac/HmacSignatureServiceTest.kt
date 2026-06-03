package com.apistarter.common.security.hmac

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HmacSignatureServiceTest {
    private val service = HmacSignatureService(
        HmacProperties(
            enabled = true,
            secret = "hmac-secret-minimum-32-characters",
        ),
    )

    @Test
    fun `sign creates verifiable hmac signature`() {
        val request = signedRequest()

        val signature = service.sign(request)

        assertThat(signature).startsWith("hmac-sha256=")
        assertThat(service.matches(request, signature)).isTrue()
    }

    @Test
    fun `tampered request does not match signature`() {
        val request = signedRequest()
        val signature = service.sign(request)
        val tamperedRequest = request.copy(body = """{"status":"FAILED"}""")

        assertThat(service.matches(tamperedRequest, signature)).isFalse()
    }

    @Test
    fun `malformed signature is rejected`() {
        assertThat(service.matches(signedRequest(), "not-a-valid-signature")).isFalse()
    }

    private fun signedRequest(): SignedRequest = SignedRequest(
        method = "POST",
        pathAndQuery = "/api/v1/webhooks/payment?order=123",
        timestamp = "2026-06-03T00:00:00Z",
        nonce = "nonce-123",
        body = """{"status":"PAID"}""",
    )
}
