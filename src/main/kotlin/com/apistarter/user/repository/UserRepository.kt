package com.apistarter.user.repository

import com.apistarter.user.domain.UserEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface UserRepository : JpaRepository<UserEntity, UUID> {
    @EntityGraph(attributePaths = ["roles"])
    fun findWithRolesById(id: UUID): Optional<UserEntity>

    @EntityGraph(attributePaths = ["roles"])
    override fun findAll(pageable: Pageable): Page<UserEntity>

    fun findByEmailIgnoreCase(email: String): Optional<UserEntity>

    fun existsByEmailIgnoreCase(email: String): Boolean
}
