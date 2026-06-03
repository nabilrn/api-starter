package com.apistarter.common.response

import java.time.Instant

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val errors: Any? = null,
    val path: String? = null,
    val timestamp: Instant = Instant.now(),
) {
    companion object {
        fun <T> success(
            data: T? = null,
            message: String = "Success",
        ): ApiResponse<T> = ApiResponse(
            success = true,
            message = message,
            data = data,
        )

        fun failure(
            message: String,
            errors: Any? = null,
            path: String? = null,
        ): ApiResponse<Nothing> = ApiResponse(
            success = false,
            message = message,
            errors = errors,
            path = path,
        )
    }
}
