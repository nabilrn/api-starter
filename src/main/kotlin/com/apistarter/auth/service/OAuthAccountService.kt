package com.apistarter.auth.service

import com.apistarter.auth.config.OAuthProperties
import com.apistarter.auth.domain.OAuthAccountEntity
import com.apistarter.auth.domain.OAuthProvider
import com.apistarter.auth.dto.AuthTokenResponse
import com.apistarter.auth.repository.OAuthAccountRepository
import com.apistarter.common.error.BadRequestException
import com.apistarter.common.error.ConflictException
import com.apistarter.common.error.NotFoundException
import com.apistarter.role.repository.RoleRepository
import com.apistarter.user.domain.UserEntity
import com.apistarter.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class OAuthAccountService(
    private val oauthAccountRepository: OAuthAccountRepository,
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val authTokenService: AuthTokenService,
    private val oauthProperties: OAuthProperties,
) {
    @Transactional
    fun authenticate(profile: OAuthProfile): AuthTokenResponse {
        ensureProviderEnabled(profile.provider)

        val existingAccount = oauthAccountRepository
            .findByProviderAndProviderUserId(profile.provider, profile.providerUserId)
            .orElse(null)

        if (existingAccount != null) {
            val user = existingAccount.user ?: throw BadRequestException("OAuth account is not linked to a user")
            if (!user.enabled) {
                throw BadRequestException("User account is disabled")
            }
            return authTokenService.issueTokens(user)
        }

        val user = userRepository.findByEmailIgnoreCase(profile.email)
            .orElseGet { createOAuthOnlyUser(profile) }

        linkAccount(user, profile)
        return authTokenService.issueTokens(user)
    }

    @Transactional
    fun linkCurrentUser(
        userId: UUID,
        profile: OAuthProfile,
    ) {
        ensureProviderEnabled(profile.provider)

        val existingAccount = oauthAccountRepository
            .findByProviderAndProviderUserId(profile.provider, profile.providerUserId)
            .orElse(null)

        if (existingAccount != null) {
            val linkedUserId = existingAccount.user?.id
            if (linkedUserId == userId) return
            throw ConflictException("OAuth account is already linked to another user")
        }

        val user = userRepository.findWithRolesById(userId)
            .orElseThrow { NotFoundException("User") }
        linkAccount(user, profile)
    }

    private fun createOAuthOnlyUser(profile: OAuthProfile): UserEntity {
        val defaultRole = roleRepository.findByName("USER")
            .orElseThrow { NotFoundException("Default role USER") }
        val user = UserEntity(
            email = profile.email,
            name = profile.name,
            passwordHash = null,
            emailVerified = true,
        )
        user.roles.add(defaultRole)
        return userRepository.save(user)
    }

    private fun linkAccount(
        user: UserEntity,
        profile: OAuthProfile,
    ) {
        oauthAccountRepository.save(
            OAuthAccountEntity(
                user = user,
                provider = profile.provider,
                providerUserId = profile.providerUserId,
                providerEmail = profile.email,
            ),
        )
    }

    private fun ensureProviderEnabled(provider: OAuthProvider) {
        if (!oauthProperties.isEnabled(provider)) {
            throw BadRequestException("OAuth provider $provider is disabled")
        }
    }
}

data class OAuthProfile(
    val provider: OAuthProvider,
    val providerUserId: String,
    val email: String,
    val name: String,
) {
    init {
        require(providerUserId.isNotBlank()) { "OAuth provider user id is required" }
        require(email.isNotBlank()) { "OAuth email is required" }
        require(name.isNotBlank()) { "OAuth display name is required" }
    }
}
