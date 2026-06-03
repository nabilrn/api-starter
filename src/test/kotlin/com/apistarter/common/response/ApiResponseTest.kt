package com.apistarter.common.response

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ApiResponseTest {
    @Test
    fun `success creates successful response`() {
        val response = ApiResponse.success(data = "value", message = "Created")

        assertThat(response.success).isTrue()
        assertThat(response.message).isEqualTo("Created")
        assertThat(response.data).isEqualTo("value")
        assertThat(response.errors).isNull()
    }

    @Test
    fun `failure creates failed response`() {
        val errors = mapOf("email" to "must be a valid email")

        val response = ApiResponse.failure(
            message = "Validation failed",
            errors = errors,
            path = "/api/v1/users",
        )

        assertThat(response.success).isFalse()
        assertThat(response.message).isEqualTo("Validation failed")
        assertThat(response.errors).isEqualTo(errors)
        assertThat(response.path).isEqualTo("/api/v1/users")
    }
}
