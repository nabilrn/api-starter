package com.apistarter.user.controller

import com.apistarter.common.pagination.PageResponse
import com.apistarter.common.response.ApiResponse
import com.apistarter.user.dto.AssignRolesRequest
import com.apistarter.user.dto.UpdateUserStatusRequest
import com.apistarter.user.dto.UserResponse
import com.apistarter.user.service.UserService
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
class AdminUserController(
    private val userService: UserService,
) {
    @GetMapping
    fun listUsers(
        @PageableDefault(size = 20) pageable: Pageable,
    ): ApiResponse<PageResponse<UserResponse>> = ApiResponse.success(
        data = PageResponse.from(userService.listUsers(pageable)),
    )

    @GetMapping("/{userId}")
    fun getUser(
        @PathVariable userId: UUID,
    ): ApiResponse<UserResponse> = ApiResponse.success(
        data = userService.getUser(userId),
    )

    @PatchMapping("/{userId}/status")
    fun updateUserStatus(
        @PathVariable userId: UUID,
        @Valid @RequestBody request: UpdateUserStatusRequest,
    ): ApiResponse<UserResponse> = ApiResponse.success(
        message = "User status updated",
        data = userService.updateUserStatus(userId, request),
    )

    @PutMapping("/{userId}/roles")
    fun assignRoles(
        @PathVariable userId: UUID,
        @Valid @RequestBody request: AssignRolesRequest,
    ): ApiResponse<UserResponse> = ApiResponse.success(
        message = "User roles updated",
        data = userService.assignRoles(userId, request),
    )
}
