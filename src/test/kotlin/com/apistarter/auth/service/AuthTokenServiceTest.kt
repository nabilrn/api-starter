package com.apistarter.auth.service

import com.apistarter.auth.config.JwtProperties
import com.apistarter.auth.repository.RefreshTokenRepository
import com.apistarter.auth.security.JwtService
import com.apistarter.auth.security.JwtToken
import com.apistarter.auth.security.TokenHashService
import com.apistarter.role.domain.RoleEntity
import com.apistarter.user.domain.UserEntity
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class AuthTokenServiceTest {
    private val refreshTokenRepository = mockk<RefreshTokenRepository>()
    private val jwtService = mockk<JwtService>()
    private val tokenHashService = mockk<TokenHashService>()
    private val service = AuthTokenService(
        refreshTokenRepository = refreshTokenRepository,
        jwtService = jwtService,
        tokenHashService = tokenHashService,
        jwtProperties = JwtProperties(
            accessSecret = "access-secret-minimum-32-characters",
            refreshSecret = "refresh-secret-minimum-32-characters",
            accessTtlMinutes = 15,
            refreshTtlDays = 30,
        ),
    )

    @Test
    fun `issue tokens creates access token and hashed refresh token`() {
        val user = UserEntity(
            email = "nabil@example.com",
            name = "Nabil",
            emailVerified = true,
        ).apply {
            id = UUID.randomUUID()
            roles.add(RoleEntity("USER"))
        }
        val savedToken = slot<com.apistarter.auth.domain.RefreshTokenEntity>()

        every { jwtService.createAccessToken(user) } returns JwtToken(
            token = "access",
            expiresAt = Instant.parse("2026-06-03T00:15:00Z"),
        )
        every { tokenHashService.sha256(any()) } returns "refresh-hash"
        every { refreshTokenRepository.save(capture(savedToken)) } answers { savedToken.captured }

        val response = service.issueTokens(user)

        assertThat(response.accessToken).isEqualTo("access")
        assertThat(response.refreshToken).isNotBlank()
        assertThat(savedToken.captured.tokenHash).isEqualTo("refresh-hash")
        assertThat(savedToken.captured.user).isEqualTo(user)
        assertThat(response.user.emailVerified).isTrue()
    }
}
