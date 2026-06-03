package com.apistarter.common.error

import jakarta.servlet.http.HttpServletRequest
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.mock.http.MockHttpInputMessage
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException

class GlobalExceptionHandlerTest {
    private val handler = GlobalExceptionHandler()
    private val request = mockk<HttpServletRequest> {
        every { requestURI } returns "/api/v1/test"
    }

    @Test
    fun `api exception keeps status message and details`() {
        val response = handler.handleApiException(
            BadRequestException("Invalid request", mapOf("field" to "error")),
            request,
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body?.success).isFalse()
        assertThat(response.body?.message).isEqualTo("Invalid request")
        assertThat(response.body?.errors).isEqualTo(mapOf("field" to "error"))
        assertThat(response.body?.path).isEqualTo("/api/v1/test")
    }

    @Test
    fun `unreadable message returns malformed body response`() {
        val response = handler.handleUnreadableMessage(request)

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body?.message).isEqualTo("Malformed request body")
    }

    @Test
    fun `authentication exception returns unauthorized response`() {
        val response = handler.handleAuthenticationException(request)

        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(response.body?.message).isEqualTo("Authentication required")
    }

    @Test
    fun `access denied returns forbidden response`() {
        val response = handler.handleAccessDeniedException(request)

        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
        assertThat(response.body?.message).isEqualTo("Access denied")
    }

    @Test
    fun `unexpected exception returns server error response`() {
        val response = handler.handleUnexpectedException(request)

        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.body?.message).isEqualTo("Unexpected server error")
    }

    @Test
    fun `exception classes expose expected statuses`() {
        assertThat(NotFoundException("User").status).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(ConflictException("Duplicate").status).isEqualTo(HttpStatus.CONFLICT)
        assertThat(BadRequestException("Bad").status).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(BadCredentialsException("Bad").message).isEqualTo("Bad")
        assertThat(
            HttpMessageNotReadableException(
                "bad",
                MockHttpInputMessage(ByteArray(0)),
            ).message,
        ).contains("bad")
    }
}
