package com.apistarter.role.repository

import com.apistarter.role.domain.RoleEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface RoleRepository : JpaRepository<RoleEntity, UUID> {
    fun findByName(name: String): Optional<RoleEntity>

    fun existsByName(name: String): Boolean
}
