package com.apistarter.auth.security

import com.apistarter.role.domain.RoleEntity
import com.apistarter.user.domain.UserEntity
import com.apistarter.user.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import java.util.Optional
import java.util.UUID

class JwtAuthenticationFilterTest {
    private val jwtService = mockk<JwtService>()
    private val userRepository = mockk<UserRepository>()
    private val filter = JwtAuthenticationFilter(jwtService, userRepository)

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `valid bearer token authenticates enabled user`() {
        val userId = UUID.randomUUID()
        val user = UserEntity(
            email = "nabil@example.com",
            name = "Nabil",
            enabled = true,
        ).apply {
            id = userId
            roles.add(RoleEntity("USER"))
        }
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer valid-token")
        }

        every { jwtService.validateAccessToken("valid-token") } returns AccessTokenClaims(userId)
        every { userRepository.findWithRolesById(userId) } returns Optional.of(user)

        filter.doFilter(request, MockHttpServletResponse(), MockFilterChain())

        assertThat(SecurityContextHolder.getContext().authentication?.name).isEqualTo("nabil@example.com")
    }

    @Test
    fun `invalid bearer token leaves request anonymous`() {
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer invalid-token")
        }

        every { jwtService.validateAccessToken("invalid-token") } returns null

        filter.doFilter(request, MockHttpServletResponse(), MockFilterChain())

        assertThat(SecurityContextHolder.getContext().authentication).isNull()
    }

    @Test
    fun `request without bearer token leaves request anonymous`() {
        filter.doFilter(MockHttpServletRequest(), MockHttpServletResponse(), MockFilterChain())

        assertThat(SecurityContextHolder.getContext().authentication).isNull()
    }
}
