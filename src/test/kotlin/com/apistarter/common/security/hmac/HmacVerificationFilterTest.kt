package com.apistarter.common.security.hmac

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import java.time.Instant

class HmacVerificationFilterTest {
    private val properties = HmacProperties(
        enabled = true,
        secret = "hmac-secret-minimum-32-characters",
        maxSkewSeconds = 300,
        protectedPaths = listOf("/api/v1/webhooks"),
    )
    private val signatureService = HmacSignatureService(properties)
    private val filter = HmacVerificationFilter(
        properties = properties,
        verifier = HmacRequestVerifier(properties, signatureService, InMemoryReplayNonceStore()),
        objectMapper = ObjectMapper().findAndRegisterModules(),
    )

    @Test
    fun `valid signed protected request continues filter chain`() {
        val body = """{"status":"PAID"}"""
        val timestamp = Instant.now().toString()
        val nonce = "nonce-1"
        val request = request(body)
        val signedRequest = SignedRequest(
            method = "POST",
            pathAndQuery = "/api/v1/webhooks/payment?order=123",
            timestamp = timestamp,
            nonce = nonce,
            body = body,
        )
        request.addHeader(HmacVerificationFilter.HMAC_TIMESTAMP_HEADER, timestamp)
        request.addHeader(HmacVerificationFilter.HMAC_NONCE_HEADER, nonce)
        request.addHeader(HmacVerificationFilter.HMAC_SIGNATURE_HEADER, signatureService.sign(signedRequest))
        val response = MockHttpServletResponse()
        val chain = MockFilterChain()

        filter.doFilter(request, response, chain)

        assertThat(response.status).isEqualTo(200)
    }

    @Test
    fun `missing signature rejects protected request`() {
        val response = MockHttpServletResponse()

        filter.doFilter(request("""{"status":"PAID"}"""), response, MockFilterChain())

        assertThat(response.status).isEqualTo(401)
        assertThat(response.contentAsString).contains("Invalid signed request")
    }

    @Test
    fun `unprotected request skips hmac verification`() {
        val request = MockHttpServletRequest("POST", "/api/v1/auth/login")
        val response = MockHttpServletResponse()

        filter.doFilter(request, response, MockFilterChain())

        assertThat(response.status).isEqualTo(200)
    }

    private fun request(body: String): MockHttpServletRequest =
        MockHttpServletRequest("POST", "/api/v1/webhooks/payment").apply {
            queryString = "order=123"
            setContent(body.toByteArray(Charsets.UTF_8))
            characterEncoding = Charsets.UTF_8.name()
        }
}
