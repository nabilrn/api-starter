package com.apistarter.auth.service

import com.apistarter.auth.config.OAuthProperties
import com.apistarter.auth.config.OAuthProviderProperties
import com.apistarter.auth.domain.OAuthAccountEntity
import com.apistarter.auth.domain.OAuthProvider
import com.apistarter.auth.dto.AuthTokenResponse
import com.apistarter.auth.dto.AuthUserResponse
import com.apistarter.auth.repository.OAuthAccountRepository
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
import java.time.Instant
import java.util.Optional
import java.util.UUID

class OAuthAccountServiceTest {
    private val oauthAccountRepository = mockk<OAuthAccountRepository>()
    private val userRepository = mockk<UserRepository>()
    private val roleRepository = mockk<RoleRepository>()
    private val authTokenService = mockk<AuthTokenService>()
    private val oauthProperties = OAuthProperties(
        google = OAuthProviderProperties(enabled = true),
        github = OAuthProviderProperties(enabled = false),
    )

    private lateinit var service: OAuthAccountService

    @BeforeEach
    fun setUp() {
        service = OAuthAccountService(
            oauthAccountRepository = oauthAccountRepository,
            userRepository = userRepository,
            roleRepository = roleRepository,
            authTokenService = authTokenService,
            oauthProperties = oauthProperties,
        )
    }

    @Test
    fun `authenticate issues tokens for existing oauth account`() {
        val user = user(UUID.randomUUID())
        val account = OAuthAccountEntity(
            user = user,
            provider = OAuthProvider.GOOGLE,
            providerUserId = "google-123",
            providerEmail = "nabil@example.com",
        )
        val profile = profile()

        every {
            oauthAccountRepository.findByProviderAndProviderUserId(OAuthProvider.GOOGLE, "google-123")
        } returns Optional.of(account)
        every { authTokenService.issueTokens(user) } returns tokenResponse(user)

        val response = service.authenticate(profile)

        assertThat(response.user.id).isEqualTo(user.id)
        assertThat(response.accessToken).isEqualTo("access")
    }

    @Test
    fun `authenticate creates oauth-only user when email is new`() {
        val profile = profile()
        val role = RoleEntity("USER")
        val savedUserSlot = slot<UserEntity>()
        val accountSlot = slot<OAuthAccountEntity>()

        every {
            oauthAccountRepository.findByProviderAndProviderUserId(OAuthProvider.GOOGLE, "google-123")
        } returns Optional.empty()
        every { userRepository.findByEmailIgnoreCase("nabil@example.com") } returns Optional.empty()
        every { roleRepository.findByName("USER") } returns Optional.of(role)
        every { userRepository.save(capture(savedUserSlot)) } answers {
            savedUserSlot.captured.apply { id = UUID.randomUUID() }
        }
        every { oauthAccountRepository.save(capture(accountSlot)) } answers { accountSlot.captured }
        every { authTokenService.issueTokens(any()) } answers { tokenResponse(firstArg()) }

        val response = service.authenticate(profile)

        assertThat(savedUserSlot.captured.passwordHash).isNull()
        assertThat(savedUserSlot.captured.emailVerified).isTrue()
        assertThat(savedUserSlot.captured.roles).containsExactly(role)
        assertThat(accountSlot.captured.provider).isEqualTo(OAuthProvider.GOOGLE)
        assertThat(response.user.email).isEqualTo("nabil@example.com")
    }

    @Test
    fun `authenticate links existing local user by email`() {
        val profile = profile()
        val existingUser = user(UUID.randomUUID())
        val accountSlot = slot<OAuthAccountEntity>()

        every {
            oauthAccountRepository.findByProviderAndProviderUserId(OAuthProvider.GOOGLE, "google-123")
        } returns Optional.empty()
        every { userRepository.findByEmailIgnoreCase("nabil@example.com") } returns Optional.of(existingUser)
        every { oauthAccountRepository.save(capture(accountSlot)) } answers { accountSlot.captured }
        every { authTokenService.issueTokens(existingUser) } returns tokenResponse(existingUser)

        service.authenticate(profile)

        assertThat(accountSlot.captured.user).isEqualTo(existingUser)
        assertThat(accountSlot.captured.providerUserId).isEqualTo("google-123")
    }

    @Test
    fun `disabled provider is rejected`() {
        assertThatThrownBy {
            service.authenticate(profile(provider = OAuthProvider.GITHUB))
        }.isInstanceOf(BadRequestException::class.java)
    }

    @Test
    fun `link current user is idempotent when account already belongs to same user`() {
        val userId = UUID.randomUUID()
        val account = OAuthAccountEntity(
            user = user(userId),
            provider = OAuthProvider.GOOGLE,
            providerUserId = "google-123",
        )

        every {
            oauthAccountRepository.findByProviderAndProviderUserId(OAuthProvider.GOOGLE, "google-123")
        } returns Optional.of(account)

        service.linkCurrentUser(userId, profile())

        verify(exactly = 0) { oauthAccountRepository.save(any()) }
    }

    @Test
    fun `link current user rejects account linked to another user`() {
        val account = OAuthAccountEntity(
            user = user(UUID.randomUUID()),
            provider = OAuthProvider.GOOGLE,
            providerUserId = "google-123",
        )

        every {
            oauthAccountRepository.findByProviderAndProviderUserId(OAuthProvider.GOOGLE, "google-123")
        } returns Optional.of(account)

        assertThatThrownBy {
            service.linkCurrentUser(UUID.randomUUID(), profile())
        }.isInstanceOf(ConflictException::class.java)
    }

    @Test
    fun `link current user saves new provider account`() {
        val userId = UUID.randomUUID()
        val currentUser = user(userId)
        val accountSlot = slot<OAuthAccountEntity>()

        every {
            oauthAccountRepository.findByProviderAndProviderUserId(OAuthProvider.GOOGLE, "google-123")
        } returns Optional.empty()
        every { userRepository.findWithRolesById(userId) } returns Optional.of(currentUser)
        every { oauthAccountRepository.save(capture(accountSlot)) } answers { accountSlot.captured }

        service.linkCurrentUser(userId, profile())

        assertThat(accountSlot.captured.user).isEqualTo(currentUser)
        assertThat(accountSlot.captured.provider).isEqualTo(OAuthProvider.GOOGLE)
    }

    private fun profile(
        provider: OAuthProvider = OAuthProvider.GOOGLE,
    ): OAuthProfile = OAuthProfile(
        provider = provider,
        providerUserId = "google-123",
        email = "nabil@example.com",
        name = "Nabil",
    )

    private fun user(id: UUID): UserEntity = UserEntity(
        email = "nabil@example.com",
        name = "Nabil",
        passwordHash = "hash",
    ).apply {
        this.id = id
        roles.add(RoleEntity("USER"))
    }

    private fun tokenResponse(user: UserEntity): AuthTokenResponse = AuthTokenResponse(
        accessToken = "access",
        refreshToken = "refresh",
        accessTokenExpiresAt = Instant.parse("2026-06-03T00:15:00Z"),
        refreshTokenExpiresAt = Instant.parse("2026-07-03T00:00:00Z"),
        user = AuthUserResponse(
            id = requireNotNull(user.id),
            email = user.email,
            name = user.name,
            roles = user.roles.map { it.name }.toSet(),
            emailVerified = user.emailVerified,
        ),
    )
}
