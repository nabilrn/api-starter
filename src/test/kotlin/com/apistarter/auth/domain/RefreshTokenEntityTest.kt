package com.apistarter.auth.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class RefreshTokenEntityTest {
    @Test
    fun `active token has future expiry and no revocation time`() {
        val now = Instant.parse("2026-06-02T00:00:00Z")
        val token = RefreshTokenEntity(
            expiresAt = now.plus(1, ChronoUnit.DAYS),
            revokedAt = null,
        )

        assertThat(token.isActive(now)).isTrue()
    }

    @Test
    fun `revoked token is inactive`() {
        val now = Instant.parse("2026-06-02T00:00:00Z")
        val token = RefreshTokenEntity(
            expiresAt = now.plus(1, ChronoUnit.DAYS),
            revokedAt = now,
        )

        assertThat(token.isActive(now)).isFalse()
    }

    @Test
    fun `expired token is inactive`() {
        val now = Instant.parse("2026-06-02T00:00:00Z")
        val token = RefreshTokenEntity(
            expiresAt = now.minus(1, ChronoUnit.SECONDS),
            revokedAt = null,
        )

        assertThat(token.isActive(now)).isFalse()
    }
}
