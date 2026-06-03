package com.apistarter.common.security.hmac

import com.apistarter.common.response.ApiResponse
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class HmacVerificationFilter(
    private val properties: HmacProperties,
    private val verifier: HmacRequestVerifier,
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        !properties.enabled || properties.protectedPaths.none { request.requestURI.startsWith(it) }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val wrappedRequest = CachedBodyHttpServletRequest(request)
        val signedRequest = SignedRequest(
            method = wrappedRequest.method,
            pathAndQuery = wrappedRequest.pathAndQuery(),
            timestamp = wrappedRequest.getHeader(HMAC_TIMESTAMP_HEADER).orEmpty(),
            nonce = wrappedRequest.getHeader(HMAC_NONCE_HEADER).orEmpty(),
            body = wrappedRequest.cachedBody.toString(Charsets.UTF_8),
        )

        when (val result = verifier.verifySignature(
            request = signedRequest,
            signature = wrappedRequest.getHeader(HMAC_SIGNATURE_HEADER).orEmpty(),
        )) {
            HmacVerificationResult.Valid -> filterChain.doFilter(wrappedRequest, response)
            is HmacVerificationResult.Invalid -> reject(response, request.requestURI, result.reason)
        }
    }

    private fun HttpServletRequest.pathAndQuery(): String =
        if (queryString.isNullOrBlank()) requestURI else "$requestURI?$queryString"

    private fun reject(
        response: HttpServletResponse,
        path: String,
        reason: String,
    ) {
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        objectMapper.writeValue(
            response.writer,
            ApiResponse.failure(
                message = "Invalid signed request",
                errors = mapOf("signature" to reason),
                path = path,
            ),
        )
    }

    companion object {
        const val HMAC_SIGNATURE_HEADER = "X-Signature"
        const val HMAC_TIMESTAMP_HEADER = "X-Timestamp"
        const val HMAC_NONCE_HEADER = "X-Nonce"
    }
}
