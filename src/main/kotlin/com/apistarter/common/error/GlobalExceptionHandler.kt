package com.apistarter.common.error

import com.apistarter.common.response.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ApiException::class)
    fun handleApiException(
        exception: ApiException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Nothing>> = errorResponse(
        status = exception.status,
        message = exception.message,
        errors = exception.details,
        request = request,
    )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        exception: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Nothing>> {
        val errors = exception.bindingResult
            .fieldErrors
            .groupBy({ it.field }, { it.defaultMessage ?: "Invalid value" })
            .mapValues { (_, messages) -> messages.first() }

        return errorResponse(
            status = HttpStatus.BAD_REQUEST,
            message = "Validation failed",
            errors = errors,
            request = request,
        )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(
        exception: ConstraintViolationException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Nothing>> {
        val errors = exception.constraintViolations.associate {
            it.propertyPath.toString() to it.message
        }

        return errorResponse(
            status = HttpStatus.BAD_REQUEST,
            message = "Validation failed",
            errors = errors,
            request = request,
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableMessage(
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Nothing>> = errorResponse(
        status = HttpStatus.BAD_REQUEST,
        message = "Malformed request body",
        request = request,
    )

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Nothing>> = errorResponse(
        status = HttpStatus.UNAUTHORIZED,
        message = "Authentication required",
        request = request,
    )

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Nothing>> = errorResponse(
        status = HttpStatus.FORBIDDEN,
        message = "Access denied",
        request = request,
    )

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Nothing>> = errorResponse(
        status = HttpStatus.INTERNAL_SERVER_ERROR,
        message = "Unexpected server error",
        request = request,
    )

    private fun errorResponse(
        status: HttpStatus,
        message: String,
        errors: Any? = null,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Nothing>> = ResponseEntity
        .status(status)
        .body(
            ApiResponse.failure(
                message = message,
                errors = errors,
                path = request.requestURI,
            ),
        )
}
