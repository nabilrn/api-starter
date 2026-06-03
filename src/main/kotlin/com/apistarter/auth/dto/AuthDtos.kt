package com.apistarter.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

data class RegisterRequest(
    @field:Email
    @field:NotBlank
    val email: String,

    @field:NotBlank
    @field:Size(min = 2, max = 160)
    val name: String,

    @field:NotBlank
    @field:Size(min = 8, max = 128)
    val password: String,
)

data class LoginRequest(
    @field:Email
    @field:NotBlank
    val email: String,

    @field:NotBlank
    val password: String,
)

data class RefreshTokenRequest(
    @field:NotBlank
    val refreshToken: String,
)

data class LogoutRequest(
    @field:NotBlank
    val refreshToken: String,
)

data class AuthTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val accessTokenExpiresAt: Instant,
    val refreshTokenExpiresAt: Instant,
    val user: AuthUserResponse,
)

data class AuthUserResponse(
    val id: UUID,
    val email: String,
    val name: String,
    val roles: Set<String>,
    val emailVerified: Boolean,
)
