package com.apistarter.common.security.hmac

import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class HmacSignatureService(
    private val properties: HmacProperties,
) {
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()

    fun sign(request: SignedRequest): String {
        val signature = hmacSha256(request.canonicalString(), properties.secret)
        return "$SIGNATURE_PREFIX${encoder.encodeToString(signature)}"
    }

    fun matches(
        request: SignedRequest,
        signatureHeader: String,
    ): Boolean {
        if (!signatureHeader.startsWith(SIGNATURE_PREFIX)) return false
        val actualSignature = runCatching {
            decoder.decode(signatureHeader.removePrefix(SIGNATURE_PREFIX))
        }.getOrNull() ?: return false
        val expectedSignature = hmacSha256(request.canonicalString(), properties.secret)
        return MessageDigest.isEqual(expectedSignature, actualSignature)
    }

    private fun hmacSha256(
        value: String,
        secret: String,
    ): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        return mac.doFinal(value.toByteArray(Charsets.UTF_8))
    }

    companion object {
        const val SIGNATURE_PREFIX = "hmac-sha256="
    }
}

data class SignedRequest(
    val method: String,
    val pathAndQuery: String,
    val timestamp: String,
    val nonce: String,
    val body: String,
) {
    fun canonicalString(): String = listOf(
        method.uppercase(),
        pathAndQuery,
        timestamp,
        nonce,
        body,
    ).joinToString("\n")
}
