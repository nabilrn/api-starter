package com.apistarter.auth.controller

import com.apistarter.auth.dto.AuthTokenResponse
import com.apistarter.auth.dto.AuthUserResponse
import com.apistarter.auth.dto.LoginRequest
import com.apistarter.auth.dto.LogoutRequest
import com.apistarter.auth.dto.RefreshTokenRequest
import com.apistarter.auth.dto.RegisterRequest
import com.apistarter.auth.security.UserPrincipal
import com.apistarter.auth.service.AuthService
import com.apistarter.common.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/register")
    fun register(
        @Valid @RequestBody request: RegisterRequest,
    ): ApiResponse<AuthTokenResponse> = ApiResponse.success(
        message = "Registration successful",
        data = authService.register(request),
    )

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ApiResponse<AuthTokenResponse> = ApiResponse.success(
        message = "Login successful",
        data = authService.login(request),
    )

    @PostMapping("/refresh")
    fun refresh(
        @Valid @RequestBody request: RefreshTokenRequest,
    ): ApiResponse<AuthTokenResponse> = ApiResponse.success(
        message = "Token refreshed",
        data = authService.refresh(request),
    )

    @PostMapping("/logout")
    fun logout(
        @Valid @RequestBody request: LogoutRequest,
    ): ApiResponse<Unit> {
        authService.logout(request)
        return ApiResponse.success(message = "Logout successful")
    }

    @GetMapping("/me")
    fun me(
        @AuthenticationPrincipal principal: UserPrincipal,
    ): ApiResponse<AuthUserResponse> = ApiResponse.success(
        data = authService.me(principal.id),
    )
}
