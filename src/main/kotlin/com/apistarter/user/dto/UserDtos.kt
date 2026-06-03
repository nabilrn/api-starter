package com.apistarter.user.dto

import com.apistarter.user.domain.UserEntity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

data class UserResponse(
    val id: UUID,
    val email: String,
    val name: String,
    val roles: Set<String>,
    val emailVerified: Boolean,
    val enabled: Boolean,
    val createdAt: Instant?,
    val updatedAt: Instant?,
)

data class UpdateProfileRequest(
    @field:NotBlank
    @field:Size(min = 2, max = 160)
    val name: String,
)

data class UpdateUserStatusRequest(
    val enabled: Boolean,
)

data class AssignRolesRequest(
    @field:Size(min = 1, message = "At least one role is required")
    val roles: Set<
        @NotBlank
        @Size(min = 2, max = 80)
        String,
    >,
)

fun UserEntity.toUserResponse(): UserResponse = UserResponse(
    id = requireNotNull(id) { "User must have an id" },
    email = email,
    name = name,
    roles = roles.map { it.name }.toSet(),
    emailVerified = emailVerified,
    enabled = enabled,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
