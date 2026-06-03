package com.apistarter.role.domain

import com.apistarter.common.persistence.AuditableEntity
import com.apistarter.user.domain.UserEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table

@Entity
@Table(name = "roles")
open class RoleEntity(
    @Column(name = "name", nullable = false, unique = true, length = 80)
    open var name: String = "",
) : AuditableEntity() {
    @ManyToMany(mappedBy = "roles")
    open var users: MutableSet<UserEntity> = mutableSetOf()
}
