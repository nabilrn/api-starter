package com.apistarter.auth.security

import com.apistarter.role.domain.RoleEntity
import com.apistarter.user.domain.UserEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class UserPrincipalTest {
    @Test
    fun `from maps enabled user roles to spring authorities`() {
        val user = UserEntity(
            email = "nabil@example.com",
            name = "Nabil",
            enabled = true,
        ).apply {
            id = UUID.fromString("11111111-1111-1111-1111-111111111111")
            roles.add(RoleEntity("USER"))
            roles.add(RoleEntity("ADMIN"))
        }

        val principal = UserPrincipal.from(user)

        assertThat(principal.id).isEqualTo(user.id)
        assertThat(principal.username).isEqualTo("nabil@example.com")
        assertThat(principal.displayName).isEqualTo("Nabil")
        assertThat(principal.password).isNull()
        assertThat(principal.isEnabled).isTrue()
        assertThat(principal.isAccountNonExpired).isTrue()
        assertThat(principal.isAccountNonLocked).isTrue()
        assertThat(principal.isCredentialsNonExpired).isTrue()
        assertThat(principal.authorities.map { it.authority })
            .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN")
    }
}
