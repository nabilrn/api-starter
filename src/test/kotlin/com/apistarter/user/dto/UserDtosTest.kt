package com.apistarter.user.dto

import com.apistarter.role.domain.RoleEntity
import com.apistarter.user.domain.UserEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class UserDtosTest {
    @Test
    fun `toUserResponse maps safe user fields`() {
        val user = UserEntity(
            email = "nabil@example.com",
            name = "Nabil",
            passwordHash = "secret-hash",
            emailVerified = true,
            enabled = true,
        ).apply {
            id = UUID.fromString("11111111-1111-1111-1111-111111111111")
            roles.add(RoleEntity("USER"))
            roles.add(RoleEntity("ADMIN"))
        }

        val response = user.toUserResponse()

        assertThat(response.id).isEqualTo(UUID.fromString("11111111-1111-1111-1111-111111111111"))
        assertThat(response.email).isEqualTo("nabil@example.com")
        assertThat(response.name).isEqualTo("Nabil")
        assertThat(response.roles).containsExactlyInAnyOrder("USER", "ADMIN")
        assertThat(response.emailVerified).isTrue()
        assertThat(response.enabled).isTrue()
    }
}
