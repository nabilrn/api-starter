package com.apistarter.auth.security

import com.apistarter.auth.config.JwtProperties
import com.apistarter.user.domain.UserEntity
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class JwtService(
    private val properties: JwtProperties,
    private val objectMapper: ObjectMapper,
) {
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()
    private val clock: Clock = Clock.systemUTC()

    fun createAccessToken(user: UserEntity): JwtToken {
        val now = Instant.now(clock)
        val expiresAt = now.plus(properties.accessTtlMinutes, ChronoUnit.MINUTES)
        val roles = user.roles.map { it.name }.sorted()
        val claims = linkedMapOf(
            "sub" to requireNotNull(user.id).toString(),
            "email" to user.email,
            "name" to user.name,
            "roles" to roles,
            "iat" to now.epochSecond,
            "exp" to expiresAt.epochSecond,
            "typ" to "access",
        )

        return JwtToken(
            token = sign(claims, properties.accessSecret),
            expiresAt = expiresAt,
        )
    }

    fun validateAccessToken(token: String): AccessTokenClaims? {
        val claims = parseAndVerify(token, properties.accessSecret) ?: return null
        if (claims["typ"] != "access") return null

        val expiresAt = (claims["exp"] as? Number)?.toLong() ?: return null
        if (!Instant.ofEpochSecond(expiresAt).isAfter(Instant.now(clock))) return null

        val subject = claims["sub"] as? String ?: return null
        val userId = runCatching { UUID.fromString(subject) }.getOrNull() ?: return null
        return AccessTokenClaims(userId = userId)
    }

    private fun sign(claims: Map<String, Any>, secret: String): String {
        val header = mapOf("alg" to "HS256", "typ" to "JWT")
        val headerPart = encodeJson(header)
        val payloadPart = encodeJson(claims)
        val signingInput = "$headerPart.$payloadPart"
        val signature = hmacSha256(signingInput, secret)
        return "$signingInput.${encoder.encodeToString(signature)}"
    }

    private fun parseAndVerify(token: String, secret: String): Map<String, Any?>? {
        val parts = token.split(".")
        if (parts.size != 3) return null

        val signingInput = "${parts[0]}.${parts[1]}"
        val expectedSignature = hmacSha256(signingInput, secret)
        val actualSignature = runCatching { decoder.decode(parts[2]) }.getOrNull() ?: return null
        if (!MessageDigestTimingSafe.equals(expectedSignature, actualSignature)) return null

        return runCatching {
            objectMapper.readValue(
                decoder.decode(parts[1]),
                object : TypeReference<Map<String, Any?>>() {},
            )
        }.getOrNull()
    }

    private fun encodeJson(value: Any): String =
        encoder.encodeToString(objectMapper.writeValueAsBytes(value))

    private fun hmacSha256(value: String, secret: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        return mac.doFinal(value.toByteArray(Charsets.UTF_8))
    }
}

data class JwtToken(
    val token: String,
    val expiresAt: Instant,
)

data class AccessTokenClaims(
    val userId: UUID,
)

private object MessageDigestTimingSafe {
    fun equals(expected: ByteArray, actual: ByteArray): Boolean {
        if (expected.size != actual.size) return false
        var result = 0
        for (index in expected.indices) {
            result = result or (expected[index].toInt() xor actual[index].toInt())
        }
        return result == 0
    }
}
