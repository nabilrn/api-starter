package com.apistarter.health

import com.apistarter.auth.security.JwtService
import com.apistarter.common.config.SecurityConfig
import com.apistarter.common.security.hmac.HmacProperties
import com.apistarter.common.security.hmac.HmacRequestVerifier
import com.apistarter.user.repository.UserRepository
import io.mockk.every
import com.ninjasquad.springmockk.MockkBean
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(HealthController::class)
@Import(SecurityConfig::class)
class HealthControllerTest(
    @Autowired private val mockMvc: MockMvc,
) {
    @MockkBean
    private lateinit var jwtService: JwtService

    @MockkBean
    private lateinit var userRepository: UserRepository

    @MockkBean
    private lateinit var hmacProperties: HmacProperties

    @MockkBean
    private lateinit var hmacRequestVerifier: HmacRequestVerifier

    @Test
    fun `health endpoint returns standard response`() {
        every { hmacProperties.enabled } returns false

        mockMvc.get("/api/v1/health")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.message") { value("API is healthy") }
                jsonPath("$.data.status") { value("UP") }
            }
    }
}
