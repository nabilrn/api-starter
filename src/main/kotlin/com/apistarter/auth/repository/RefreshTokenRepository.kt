package com.apistarter.auth.repository

import com.apistarter.auth.domain.RefreshTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface RefreshTokenRepository : JpaRepository<RefreshTokenEntity, UUID> {
    fun findByTokenHash(tokenHash: String): Optional<RefreshTokenEntity>

    fun deleteByUser_Id(userId: UUID)
}
