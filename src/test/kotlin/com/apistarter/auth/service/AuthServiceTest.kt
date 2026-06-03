package com.apistarter.auth.service

import com.apistarter.auth.domain.RefreshTokenEntity
import com.apistarter.auth.dto.LoginRequest
import com.apistarter.auth.dto.LogoutRequest
import com.apistarter.auth.dto.RefreshTokenRequest
import com.apistarter.auth.dto.RegisterRequest
import com.apistarter.auth.repository.RefreshTokenRepository
import com.apistarter.auth.security.TokenHashService
import com.apistarter.common.error.BadRequestException
import com.apistarter.common.error.ConflictException
import com.apistarter.role.domain.RoleEntity
import com.apistarter.role.repository.RoleRepository
import com.apistarter.user.domain.UserEntity
import com.apistarter.user.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Optional
import java.util.UUID

class AuthServiceTest {
    private val userRepository = mockk<UserRepository>()
    private val roleRepository = mockk<RoleRepository>()
    private val refreshTokenRepository = mockk<RefreshTokenRepository>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val authTokenService = mockk<AuthTokenService>()
    private val tokenHashService = mockk<TokenHashService>()

    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        authService = AuthService(
            userRepository = userRepository,
            roleRepository = roleRepository,
            refreshTokenRepository = refreshTokenRepository,
            passwordEncoder = passwordEncoder,
            authTokenService = authTokenService,
            tokenHashService = tokenHashService,
        )
    }

    @Test
    fun `register creates user with default role and issues tokens`() {
        val userId = UUID.randomUUID()
        val role = RoleEntity("USER")
        val savedUserSlot = slot<UserEntity>()

        every { userRepository.existsByEmailIgnoreCase("nabil@example.com") } returns false
        every { roleRepository.findByName("USER") } returns Optional.of(role)
        every { passwordEncoder.encode("password123") } returns "hashed-password"
        every { userRepository.save(capture(savedUserSlot)) } answers {
            savedUserSlot.captured.apply { id = userId }
        }
        every { authTokenService.issueTokens(any()) } returns tokenResponse(userId)

        val response = authService.register(
            RegisterRequest(
                email = " NABIL@example.com ",
                name = " Nabil ",
                password = "password123",
            ),
        )

        assertThat(savedUserSlot.captured.email).isEqualTo("nabil@example.com")
        assertThat(savedUserSlot.captured.name).isEqualTo("Nabil")
        assertThat(savedUserSlot.captured.passwordHash).isEqualTo("hashed-password")
        assertThat(savedUserSlot.captured.roles).containsExactly(role)
        assertThat(response.accessToken).isEqualTo("access-token")
        assertThat(response.refreshToken).isEqualTo("refresh-token")
        assertThat(response.user.id).isEqualTo(userId)
    }

    @Test
    fun `register rejects duplicate email`() {
        every { userRepository.existsByEmailIgnoreCase("nabil@example.com") } returns true

        assertThatThrownBy {
            authService.register(
                RegisterRequest(
                    email = "nabil@example.com",
                    name = "Nabil",
                    password = "password123",
                ),
            )
        }.isInstanceOf(ConflictException::class.java)
    }

    @Test
    fun `login issues tokens for valid password`() {
        val user = user(id = UUID.randomUUID(), passwordHash = "hashed-password")

        every { userRepository.findByEmailIgnoreCase("nabil@example.com") } returns Optional.of(user)
        every { passwordEncoder.matches("password123", "hashed-password") } returns true
        every { authTokenService.issueTokens(user) } returns tokenResponse(requireNotNull(user.id))

        val response = authService.login(LoginRequest("nabil@example.com", "password123"))

        assertThat(response.accessToken).isEqualTo("access-token")
        assertThat(response.user.email).isEqualTo("nabil@example.com")
    }

    @Test
    fun `login rejects invalid password`() {
        val user = user(id = UUID.randomUUID(), passwordHash = "hashed-password")

        every { userRepository.findByEmailIgnoreCase("nabil@example.com") } returns Optional.of(user)
        every { passwordEncoder.matches("wrong-password", "hashed-password") } returns false

        assertThatThrownBy {
            authService.login(LoginRequest("nabil@example.com", "wrong-password"))
        }.isInstanceOf(BadCredentialsException::class.java)
    }

    @Test
    fun `refresh revokes current token and issues replacement`() {
        val user = user(id = UUID.randomUUID(), passwordHash = "hashed-password")
        val refreshToken = RefreshTokenEntity(
            user = user,
            tokenHash = "current-hash",
            expiresAt = Instant.now().plus(1, ChronoUnit.DAYS),
        )

        every { tokenHashService.sha256("current-refresh-token") } returns "current-hash"
        every { refreshTokenRepository.findByTokenHash("current-hash") } returns Optional.of(refreshToken)
        every { refreshTokenRepository.save(any()) } answers { firstArg() }
        every { authTokenService.issueTokens(user) } returns tokenResponse(requireNotNull(user.id), accessToken = "new-access-token")

        val response = authService.refresh(RefreshTokenRequest("current-refresh-token"))

        assertThat(refreshToken.revokedAt).isNotNull()
        assertThat(response.accessToken).isEqualTo("new-access-token")
        verify(exactly = 1) { refreshTokenRepository.save(refreshToken) }
    }

    @Test
    fun `refresh rejects missing token`() {
        every { tokenHashService.sha256("missing") } returns "missing-hash"
        every { refreshTokenRepository.findByTokenHash("missing-hash") } returns Optional.empty()

        assertThatThrownBy {
            authService.refresh(RefreshTokenRequest("missing"))
        }.isInstanceOf(BadRequestException::class.java)
    }

    @Test
    fun `logout revokes active token`() {
        val refreshToken = RefreshTokenEntity(
            user = user(id = UUID.randomUUID(), passwordHash = "hashed-password"),
            tokenHash = "current-hash",
            expiresAt = Instant.now().plus(1, ChronoUnit.DAYS),
        )

        every { tokenHashService.sha256("refresh-token") } returns "current-hash"
        every { refreshTokenRepository.findByTokenHash("current-hash") } returns Optional.of(refreshToken)
        every { refreshTokenRepository.save(refreshToken) } returns refreshToken

        authService.logout(LogoutRequest("refresh-token"))

        assertThat(refreshToken.revokedAt).isNotNull()
        verify { refreshTokenRepository.save(refreshToken) }
    }

    private fun tokenResponse(
        userId: UUID,
        accessToken: String = "access-token",
    ) = com.apistarter.auth.dto.AuthTokenResponse(
        accessToken = accessToken,
        refreshToken = "refresh-token",
        accessTokenExpiresAt = Instant.parse("2026-06-03T00:15:00Z"),
        refreshTokenExpiresAt = Instant.parse("2026-07-03T00:00:00Z"),
        user = com.apistarter.auth.dto.AuthUserResponse(
            id = userId,
            email = "nabil@example.com",
            name = "Nabil",
            roles = setOf("USER"),
            emailVerified = false,
        ),
    )

    private fun user(
        id: UUID,
        passwordHash: String?,
    ): UserEntity = UserEntity(
        email = "nabil@example.com",
        name = "Nabil",
        passwordHash = passwordHash,
    ).apply {
        this.id = id
        roles.add(RoleEntity("USER"))
    }
}
