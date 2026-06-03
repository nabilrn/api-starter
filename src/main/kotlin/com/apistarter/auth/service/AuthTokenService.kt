package com.apistarter.auth.service

import com.apistarter.auth.config.JwtProperties
import com.apistarter.auth.domain.RefreshTokenEntity
import com.apistarter.auth.dto.AuthTokenResponse
import com.apistarter.auth.dto.AuthUserResponse
import com.apistarter.auth.repository.RefreshTokenRepository
import com.apistarter.auth.security.JwtService
import com.apistarter.auth.security.TokenHashService
import com.apistarter.user.domain.UserEntity
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64

@Service
class AuthTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtService: JwtService,
    private val tokenHashService: TokenHashService,
    private val jwtProperties: JwtProperties,
) {
    private val secureRandom = SecureRandom()
    private val refreshTokenEncoder = Base64.getUrlEncoder().withoutPadding()

    fun issueTokens(user: UserEntity): AuthTokenResponse {
        val accessToken = jwtService.createAccessToken(user)
        val refreshToken = createRefreshToken(user)
        return AuthTokenResponse(
            accessToken = accessToken.token,
            refreshToken = refreshToken.rawToken,
            accessTokenExpiresAt = accessToken.expiresAt,
            refreshTokenExpiresAt = refreshToken.expiresAt,
            user = toAuthUserResponse(user),
        )
    }

    fun toAuthUserResponse(user: UserEntity): AuthUserResponse = AuthUserResponse(
        id = requireNotNull(user.id) { "User must have an id" },
        email = user.email,
        name = user.name,
        roles = user.roles.map { it.name }.toSet(),
        emailVerified = user.emailVerified,
    )

    private fun createRefreshToken(user: UserEntity): CreatedRefreshToken {
        val rawToken = generateOpaqueToken()
        val expiresAt = Instant.now().plus(jwtProperties.refreshTtlDays, ChronoUnit.DAYS)
        refreshTokenRepository.save(
            RefreshTokenEntity(
                user = user,
                tokenHash = tokenHashService.sha256(rawToken),
                expiresAt = expiresAt,
            ),
        )

        return CreatedRefreshToken(rawToken = rawToken, expiresAt = expiresAt)
    }

    private fun generateOpaqueToken(): String {
        val bytes = ByteArray(64)
        secureRandom.nextBytes(bytes)
        return refreshTokenEncoder.encodeToString(bytes)
    }
}

private data class CreatedRefreshToken(
    val rawToken: String,
    val expiresAt: Instant,
)
