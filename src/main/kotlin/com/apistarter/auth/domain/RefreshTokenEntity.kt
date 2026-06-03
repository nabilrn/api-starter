package com.apistarter.auth.domain

import com.apistarter.common.persistence.AuditableEntity
import com.apistarter.user.domain.UserEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "refresh_tokens")
open class RefreshTokenEntity(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    open var user: UserEntity? = null,

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    open var tokenHash: String = "",

    @Column(name = "expires_at", nullable = false)
    open var expiresAt: Instant = Instant.EPOCH,

    @Column(name = "revoked_at")
    open var revokedAt: Instant? = null,
) : AuditableEntity() {
    fun isActive(now: Instant = Instant.now()): Boolean =
        revokedAt == null && expiresAt.isAfter(now)
}
