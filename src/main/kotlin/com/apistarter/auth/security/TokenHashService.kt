package com.apistarter.auth.security

import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.util.Base64

@Service
class TokenHashService {
    fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }
}
