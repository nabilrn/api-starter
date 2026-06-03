package com.apistarter.user.service

import com.apistarter.common.error.BadRequestException
import com.apistarter.common.error.NotFoundException
import com.apistarter.role.domain.RoleEntity
import com.apistarter.role.repository.RoleRepository
import com.apistarter.user.domain.UserEntity
import com.apistarter.user.dto.AssignRolesRequest
import com.apistarter.user.dto.UpdateProfileRequest
import com.apistarter.user.dto.UpdateUserStatusRequest
import com.apistarter.user.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.Optional
import java.util.UUID

class UserServiceTest {
    private val userRepository = mockk<UserRepository>()
    private val roleRepository = mockk<RoleRepository>()

    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userService = UserService(
            userRepository = userRepository,
            roleRepository = roleRepository,
        )
    }

    @Test
    fun `get current user returns mapped user`() {
        val userId = UUID.randomUUID()
        every { userRepository.findWithRolesById(userId) } returns Optional.of(user(userId))

        val response = userService.getCurrentUser(userId)

        assertThat(response.id).isEqualTo(userId)
        assertThat(response.roles).containsExactly("USER")
    }

    @Test
    fun `update current user trims and saves display name`() {
        val userId = UUID.randomUUID()
        val user = user(userId)
        val savedUserSlot = slot<UserEntity>()

        every { userRepository.findWithRolesById(userId) } returns Optional.of(user)
        every { userRepository.save(capture(savedUserSlot)) } answers { savedUserSlot.captured }

        val response = userService.updateCurrentUser(
            userId,
            UpdateProfileRequest(" Updated Name "),
        )

        assertThat(savedUserSlot.captured.name).isEqualTo("Updated Name")
        assertThat(response.name).isEqualTo("Updated Name")
    }

    @Test
    fun `list users maps paged users`() {
        val pageable = PageRequest.of(0, 20)
        every { userRepository.findAll(pageable) } returns PageImpl(listOf(user(UUID.randomUUID())))

        val response = userService.listUsers(pageable)

        assertThat(response.content).hasSize(1)
        assertThat(response.content.first().email).isEqualTo("nabil@example.com")
    }

    @Test
    fun `update user status saves enabled flag`() {
        val userId = UUID.randomUUID()
        val user = user(userId)

        every { userRepository.findWithRolesById(userId) } returns Optional.of(user)
        every { userRepository.save(user) } returns user

        val response = userService.updateUserStatus(userId, UpdateUserStatusRequest(enabled = false))

        assertThat(user.enabled).isFalse()
        assertThat(response.enabled).isFalse()
    }

    @Test
    fun `assign roles normalizes role names and replaces existing roles`() {
        val userId = UUID.randomUUID()
        val user = user(userId)
        val adminRole = RoleEntity("ADMIN")

        every { userRepository.findWithRolesById(userId) } returns Optional.of(user)
        every { roleRepository.findByName("ADMIN") } returns Optional.of(adminRole)
        every { userRepository.save(user) } returns user

        val response = userService.assignRoles(userId, AssignRolesRequest(setOf(" admin ")))

        assertThat(user.roles).containsExactly(adminRole)
        assertThat(response.roles).containsExactly("ADMIN")
    }

    @Test
    fun `assign roles rejects empty role set`() {
        assertThatThrownBy {
            userService.assignRoles(UUID.randomUUID(), AssignRolesRequest(emptySet()))
        }.isInstanceOf(BadRequestException::class.java)
    }

    @Test
    fun `missing user throws not found`() {
        val userId = UUID.randomUUID()
        every { userRepository.findWithRolesById(userId) } returns Optional.empty()

        assertThatThrownBy {
            userService.getUser(userId)
        }.isInstanceOf(NotFoundException::class.java)
    }

    private fun user(id: UUID): UserEntity = UserEntity(
        email = "nabil@example.com",
        name = "Nabil",
        passwordHash = "hash",
    ).apply {
        this.id = id
        roles.add(RoleEntity("USER"))
    }
}
