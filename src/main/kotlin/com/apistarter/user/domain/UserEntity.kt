package com.apistarter.user.domain

import com.apistarter.auth.domain.OAuthAccountEntity
import com.apistarter.auth.domain.RefreshTokenEntity
import com.apistarter.common.persistence.AuditableEntity
import com.apistarter.role.domain.RoleEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "users")
open class UserEntity(
    @Column(name = "email", nullable = false, length = 320)
    open var email: String = "",

    @Column(name = "name", nullable = false, length = 160)
    open var name: String = "",

    @Column(name = "password_hash", length = 255)
    open var passwordHash: String? = null,

    @Column(name = "email_verified", nullable = false)
    open var emailVerified: Boolean = false,

    @Column(name = "enabled", nullable = false)
    open var enabled: Boolean = true,
) : AuditableEntity() {
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")],
    )
    open var roles: MutableSet<RoleEntity> = mutableSetOf()

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    open var refreshTokens: MutableSet<RefreshTokenEntity> = mutableSetOf()

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    open var oauthAccounts: MutableSet<OAuthAccountEntity> = mutableSetOf()
}
