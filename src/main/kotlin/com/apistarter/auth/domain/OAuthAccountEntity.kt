package com.apistarter.auth.domain

import com.apistarter.common.persistence.AuditableEntity
import com.apistarter.user.domain.UserEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "oauth_accounts",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_oauth_accounts_provider_user",
            columnNames = ["provider", "provider_user_id"],
        ),
        UniqueConstraint(
            name = "uk_oauth_accounts_user_provider",
            columnNames = ["user_id", "provider"],
        ),
    ],
)
open class OAuthAccountEntity(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    open var user: UserEntity? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 40)
    open var provider: OAuthProvider = OAuthProvider.GOOGLE,

    @Column(name = "provider_user_id", nullable = false, length = 255)
    open var providerUserId: String = "",

    @Column(name = "provider_email", length = 320)
    open var providerEmail: String? = null,
) : AuditableEntity()
