package com.apistarter.auth.security

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TokenHashServiceTest {
    private val tokenHashService = TokenHashService()

    @Test
    fun `sha256 returns deterministic url-safe hash`() {
        val first = tokenHashService.sha256("refresh-token")
        val second = tokenHashService.sha256("refresh-token")

        assertThat(first).isEqualTo(second)
        assertThat(first).doesNotContain("+", "/", "=")
    }

    @Test
    fun `different tokens produce different hashes`() {
        assertThat(tokenHashService.sha256("one"))
            .isNotEqualTo(tokenHashService.sha256("two"))
    }
}
