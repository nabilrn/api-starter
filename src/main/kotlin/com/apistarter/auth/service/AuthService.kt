package com.apistarter.auth.service

import com.apistarter.auth.dto.AuthTokenResponse
import com.apistarter.auth.dto.AuthUserResponse
import com.apistarter.auth.dto.LoginRequest
import com.apistarter.auth.dto.LogoutRequest
import com.apistarter.auth.dto.RefreshTokenRequest
import com.apistarter.auth.dto.RegisterRequest
import com.apistarter.auth.repository.RefreshTokenRepository
import com.apistarter.auth.security.TokenHashService
import com.apistarter.common.error.BadRequestException
import com.apistarter.common.error.ConflictException
import com.apistarter.common.error.NotFoundException
import com.apistarter.role.repository.RoleRepository
import com.apistarter.user.domain.UserEntity
import com.apistarter.user.repository.UserRepository
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authTokenService: AuthTokenService,
    private val tokenHashService: TokenHashService,
) {
    @Transactional
    fun register(request: RegisterRequest): AuthTokenResponse {
        val email = request.email.trim().lowercase()
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw ConflictException("Email is already registered")
        }

        val defaultRole = roleRepository.findByName("USER")
            .orElseThrow { NotFoundException("Default role USER") }
        val user = UserEntity(
            email = email,
            name = request.name.trim(),
            passwordHash = passwordEncoder.encode(request.password),
        )
        user.roles.add(defaultRole)

        return authTokenService.issueTokens(userRepository.save(user))
    }

    @Transactional
    fun login(request: LoginRequest): AuthTokenResponse {
        val user = userRepository.findByEmailIgnoreCase(request.email.trim())
            .orElseThrow { BadCredentialsException("Invalid email or password") }

        if (!user.enabled || user.passwordHash == null) {
            throw BadCredentialsException("Invalid email or password")
        }

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw BadCredentialsException("Invalid email or password")
        }

        return authTokenService.issueTokens(user)
    }

    @Transactional
    fun refresh(request: RefreshTokenRequest): AuthTokenResponse {
        val tokenHash = tokenHashService.sha256(request.refreshToken)
        val refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow { BadRequestException("Invalid refresh token") }

        if (!refreshToken.isActive()) {
            throw BadRequestException("Invalid refresh token")
        }

        val user = refreshToken.user ?: throw BadRequestException("Invalid refresh token")
        if (!user.enabled) {
            throw BadRequestException("Invalid refresh token")
        }

        refreshToken.revokedAt = Instant.now()
        refreshTokenRepository.save(refreshToken)

        return authTokenService.issueTokens(user)
    }

    @Transactional
    fun logout(request: LogoutRequest) {
        val tokenHash = tokenHashService.sha256(request.refreshToken)
        val refreshToken = refreshTokenRepository.findByTokenHash(tokenHash).orElse(null) ?: return
        if (refreshToken.revokedAt == null) {
            refreshToken.revokedAt = Instant.now()
            refreshTokenRepository.save(refreshToken)
        }
    }

    @Transactional(readOnly = true)
    fun me(userId: UUID): AuthUserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { NotFoundException("User") }
        return authTokenService.toAuthUserResponse(user)
    }
}
