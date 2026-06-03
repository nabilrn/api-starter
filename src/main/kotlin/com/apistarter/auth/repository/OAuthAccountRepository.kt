package com.apistarter.auth.repository

import com.apistarter.auth.domain.OAuthAccountEntity
import com.apistarter.auth.domain.OAuthProvider
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface OAuthAccountRepository : JpaRepository<OAuthAccountEntity, UUID> {
    fun findByProviderAndProviderUserId(
        provider: OAuthProvider,
        providerUserId: String,
    ): Optional<OAuthAccountEntity>

    fun existsByProviderAndProviderUserId(
        provider: OAuthProvider,
        providerUserId: String,
    ): Boolean

    fun findAllByUser_Id(userId: UUID): List<OAuthAccountEntity>
}
