package com.apistarter.user.controller

import com.apistarter.auth.security.UserPrincipal
import com.apistarter.common.response.ApiResponse
import com.apistarter.user.dto.UpdateProfileRequest
import com.apistarter.user.dto.UserResponse
import com.apistarter.user.service.UserService
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
) {
    @GetMapping("/me")
    fun me(
        @AuthenticationPrincipal principal: UserPrincipal,
    ): ApiResponse<UserResponse> = ApiResponse.success(
        data = userService.getCurrentUser(principal.id),
    )

    @PatchMapping("/me")
    fun updateMe(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: UpdateProfileRequest,
    ): ApiResponse<UserResponse> = ApiResponse.success(
        message = "Profile updated",
        data = userService.updateCurrentUser(principal.id, request),
    )
}
