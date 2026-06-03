package com.apistarter.auth.security

import com.apistarter.user.domain.UserEntity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.UUID

data class UserPrincipal(
    val id: UUID,
    private val email: String,
    val displayName: String,
    val roles: Set<String>,
    private val enabled: Boolean,
) : UserDetails {
    private val grantedAuthorities: Set<GrantedAuthority> =
        roles.map { SimpleGrantedAuthority("ROLE_$it") as GrantedAuthority }.toSet()

    override fun getAuthorities(): Collection<GrantedAuthority> = grantedAuthorities

    override fun getPassword(): String? = null

    override fun getUsername(): String = email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = enabled

    companion object {
        fun from(user: UserEntity): UserPrincipal = UserPrincipal(
            id = requireNotNull(user.id) { "Authenticated user must have an id" },
            email = user.email,
            displayName = user.name,
            roles = user.roles.map { it.name }.toSet(),
            enabled = user.enabled,
        )
    }
}
