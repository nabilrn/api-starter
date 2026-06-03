package com.apistarter.user.controller

import com.apistarter.auth.security.UserPrincipal
import com.apistarter.user.dto.AssignRolesRequest
import com.apistarter.user.dto.UpdateProfileRequest
import com.apistarter.user.dto.UpdateUserStatusRequest
import com.apistarter.user.dto.UserResponse
import com.apistarter.user.service.UserService
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.Instant
import java.util.UUID

class UserControllersTest {
    private val userService = mockk<UserService>()
    private val userController = UserController(userService)
    private val adminController = AdminUserController(userService)

    @Test
    fun `current user endpoint returns profile`() {
        val userId = UUID.randomUUID()
        val principal = principal(userId)
        every { userService.getCurrentUser(userId) } returns userResponse(userId)

        val response = userController.me(principal)

        assertThat(response.data?.id).isEqualTo(userId)
    }

    @Test
    fun `update current user endpoint returns updated profile`() {
        val userId = UUID.randomUUID()
        val request = UpdateProfileRequest("Updated")
        every { userService.updateCurrentUser(userId, request) } returns userResponse(userId, "Updated")

        val response = userController.updateMe(principal(userId), request)

        assertThat(response.message).isEqualTo("Profile updated")
        assertThat(response.data?.name).isEqualTo("Updated")
    }

    @Test
    fun `admin list users returns page response`() {
        val pageable = PageRequest.of(0, 20)
        every { userService.listUsers(pageable) } returns PageImpl(listOf(userResponse(UUID.randomUUID())), pageable, 1)

        val response = adminController.listUsers(pageable)

        assertThat(response.data?.items).hasSize(1)
        assertThat(response.data?.totalItems).isEqualTo(1)
    }

    @Test
    fun `admin get user returns detail`() {
        val userId = UUID.randomUUID()
        every { userService.getUser(userId) } returns userResponse(userId)

        val response = adminController.getUser(userId)

        assertThat(response.data?.id).isEqualTo(userId)
    }

    @Test
    fun `admin status endpoint updates user status`() {
        val userId = UUID.randomUUID()
        val request = UpdateUserStatusRequest(enabled = false)
        every { userService.updateUserStatus(userId, request) } returns userResponse(userId, enabled = false)

        val response = adminController.updateUserStatus(userId, request)

        assertThat(response.message).isEqualTo("User status updated")
        assertThat(response.data?.enabled).isFalse()
    }

    @Test
    fun `admin role endpoint assigns roles`() {
        val userId = UUID.randomUUID()
        val request = AssignRolesRequest(setOf("ADMIN"))
        every { userService.assignRoles(userId, request) } returns userResponse(userId, roles = setOf("ADMIN"))

        val response = adminController.assignRoles(userId, request)

        assertThat(response.message).isEqualTo("User roles updated")
        assertThat(response.data?.roles).containsExactly("ADMIN")
    }

    private fun principal(userId: UUID): UserPrincipal = UserPrincipal(
        id = userId,
        email = "nabil@example.com",
        displayName = "Nabil",
        roles = setOf("USER"),
        enabled = true,
    )

    private fun userResponse(
        userId: UUID,
        name: String = "Nabil",
        enabled: Boolean = true,
        roles: Set<String> = setOf("USER"),
    ): UserResponse = UserResponse(
        id = userId,
        email = "nabil@example.com",
        name = name,
        roles = roles,
        emailVerified = false,
        enabled = enabled,
        createdAt = Instant.parse("2026-06-03T00:00:00Z"),
        updatedAt = Instant.parse("2026-06-03T00:00:00Z"),
    )
}
