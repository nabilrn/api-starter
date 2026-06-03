package com.apistarter.auth.controller

import com.apistarter.auth.dto.AuthTokenResponse
import com.apistarter.auth.dto.AuthUserResponse
import com.apistarter.auth.dto.LoginRequest
import com.apistarter.auth.dto.LogoutRequest
import com.apistarter.auth.dto.RefreshTokenRequest
import com.apistarter.auth.dto.RegisterRequest
import com.apistarter.auth.security.UserPrincipal
import com.apistarter.auth.service.AuthService
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class AuthControllerTest {
    private val authService = mockk<AuthService>()
    private val controller = AuthController(authService)

    @Test
    fun `register delegates to auth service`() {
        val request = RegisterRequest("nabil@example.com", "Nabil", "password123")
        every { authService.register(request) } returns tokenResponse()

        val response = controller.register(request)

        assertThat(response.success).isTrue()
        assertThat(response.message).isEqualTo("Registration successful")
        assertThat(response.data?.accessToken).isEqualTo("access")
    }

    @Test
    fun `login delegates to auth service`() {
        val request = LoginRequest("nabil@example.com", "password123")
        every { authService.login(request) } returns tokenResponse()

        val response = controller.login(request)

        assertThat(response.message).isEqualTo("Login successful")
        assertThat(response.data?.refreshToken).isEqualTo("refresh")
    }

    @Test
    fun `refresh delegates to auth service`() {
        val request = RefreshTokenRequest("refresh")
        every { authService.refresh(request) } returns tokenResponse()

        val response = controller.refresh(request)

        assertThat(response.message).isEqualTo("Token refreshed")
    }

    @Test
    fun `logout revokes refresh token`() {
        val request = LogoutRequest("refresh")
        justRun { authService.logout(request) }

        val response = controller.logout(request)

        assertThat(response.message).isEqualTo("Logout successful")
        verify { authService.logout(request) }
    }

    @Test
    fun `me returns current auth user`() {
        val userId = UUID.randomUUID()
        val principal = UserPrincipal(
            id = userId,
            email = "nabil@example.com",
            displayName = "Nabil",
            roles = setOf("USER"),
            enabled = true,
        )
        every { authService.me(userId) } returns authUser(userId)

        val response = controller.me(principal)

        assertThat(response.data?.id).isEqualTo(userId)
    }

    private fun tokenResponse(): AuthTokenResponse {
        val userId = UUID.randomUUID()
        return AuthTokenResponse(
            accessToken = "access",
            refreshToken = "refresh",
            accessTokenExpiresAt = Instant.parse("2026-06-03T00:15:00Z"),
            refreshTokenExpiresAt = Instant.parse("2026-07-03T00:00:00Z"),
            user = authUser(userId),
        )
    }

    private fun authUser(userId: UUID): AuthUserResponse = AuthUserResponse(
        id = userId,
        email = "nabil@example.com",
        name = "Nabil",
        roles = setOf("USER"),
        emailVerified = false,
    )
}
