package com.apistarter.user.service

import com.apistarter.common.error.BadRequestException
import com.apistarter.common.error.NotFoundException
import com.apistarter.role.repository.RoleRepository
import com.apistarter.user.dto.AssignRolesRequest
import com.apistarter.user.dto.UpdateProfileRequest
import com.apistarter.user.dto.UpdateUserStatusRequest
import com.apistarter.user.dto.UserResponse
import com.apistarter.user.dto.toUserResponse
import com.apistarter.user.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
) {
    @Transactional(readOnly = true)
    fun getCurrentUser(userId: UUID): UserResponse = findUserWithRoles(userId).toUserResponse()

    @Transactional
    fun updateCurrentUser(
        userId: UUID,
        request: UpdateProfileRequest,
    ): UserResponse {
        val user = findUserWithRoles(userId)
        user.name = request.name.trim()
        return userRepository.save(user).toUserResponse()
    }

    @Transactional(readOnly = true)
    fun listUsers(pageable: Pageable): Page<UserResponse> =
        userRepository.findAll(pageable).map { it.toUserResponse() }

    @Transactional(readOnly = true)
    fun getUser(userId: UUID): UserResponse = findUserWithRoles(userId).toUserResponse()

    @Transactional
    fun updateUserStatus(
        userId: UUID,
        request: UpdateUserStatusRequest,
    ): UserResponse {
        val user = findUserWithRoles(userId)
        user.enabled = request.enabled
        return userRepository.save(user).toUserResponse()
    }

    @Transactional
    fun assignRoles(
        userId: UUID,
        request: AssignRolesRequest,
    ): UserResponse {
        if (request.roles.isEmpty()) {
            throw BadRequestException("At least one role is required")
        }

        val normalizedRoles = request.roles.map { it.trim().uppercase() }.toSet()
        val roles = normalizedRoles.map { roleName ->
            roleRepository.findByName(roleName)
                .orElseThrow { NotFoundException("Role $roleName") }
        }.toMutableSet()

        val user = findUserWithRoles(userId)
        user.roles.clear()
        user.roles.addAll(roles)

        return userRepository.save(user).toUserResponse()
    }

    private fun findUserWithRoles(userId: UUID) =
        userRepository.findWithRolesById(userId)
            .orElseThrow { NotFoundException("User") }
}
