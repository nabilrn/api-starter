package com.apistarter.common.security.hmac

import org.springframework.stereotype.Component
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

interface ReplayNonceStore {
    fun remember(
        nonce: String,
        expiresAt: Instant,
    ): Boolean
}

@Component
class InMemoryReplayNonceStore : ReplayNonceStore {
    private val nonces = ConcurrentHashMap<String, Instant>()

    override fun remember(
        nonce: String,
        expiresAt: Instant,
    ): Boolean {
        cleanupExpired()
        return nonces.putIfAbsent(nonce, expiresAt) == null
    }

    private fun cleanupExpired(now: Instant = Instant.now()) {
        nonces.entries.removeIf { (_, expiresAt) -> !expiresAt.isAfter(now) }
    }
}
