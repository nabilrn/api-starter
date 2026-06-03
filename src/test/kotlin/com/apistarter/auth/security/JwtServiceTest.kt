package com.apistarter.auth.security

import com.apistarter.auth.config.JwtProperties
import com.apistarter.role.domain.RoleEntity
import com.apistarter.user.domain.UserEntity
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class JwtServiceTest {
    private val jwtService = JwtService(
        properties = JwtProperties(
            accessSecret = "test-access-secret-minimum-32-characters",
            refreshSecret = "test-refresh-secret-minimum-32-characters",
            accessTtlMinutes = 15,
            refreshTtlDays = 30,
        ),
        objectMapper = jacksonObjectMapper(),
    )

    @Test
    fun `created access token validates to user id`() {
        val userId = UUID.randomUUID()
        val user = UserEntity(
            email = "nabil@example.com",
            name = "Nabil",
        ).apply {
            id = userId
            roles.add(RoleEntity("USER"))
        }

        val token = jwtService.createAccessToken(user)
        val claims = jwtService.validateAccessToken(token.token)

        assertThat(claims?.userId).isEqualTo(userId)
    }

    @Test
    fun `tampered access token is rejected`() {
        val user = UserEntity(
            email = "nabil@example.com",
            name = "Nabil",
        ).apply {
            id = UUID.randomUUID()
        }

        val token = jwtService.createAccessToken(user)
        val tamperedToken = token.token.replaceAfterLast(".", "invalid")

        assertThat(jwtService.validateAccessToken(tamperedToken)).isNull()
    }
}
