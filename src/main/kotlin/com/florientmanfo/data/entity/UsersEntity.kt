package com.florientmanfo.com.florientmanfo.data.entity

import com.florientmanfo.com.florientmanfo.data.table.Users
import com.florientmanfo.com.florientmanfo.models.user.UserModel
import com.florientmanfo.com.florientmanfo.models.user.UserRole
import com.florientmanfo.com.florientmanfo.utils.toLong
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

class UsersEntity(id: EntityID<String>) : Entity<String>(id) {

    companion object : EntityClass<String, UsersEntity>(Users)

    var name by Users.name
    var email by Users.email
    var password by Users.password
    var role by Users.role
    var activated by Users.activated
    var createdAt by Users.createdAt
    var updatedAt by Users.updatedAt

    fun toModel(): UserModel {
        return UserModel(
            id = id.value,
            name = name,
            email = email,
            password = password,
            role = UserRole.valueOf(role),
            activated = activated,
            createdAt = createdAt.toLong(),
            updatedAt = updatedAt.toLong()
        )
    }
}