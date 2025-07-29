package com.florientmanfo.com.florientmanfo.data.entity

import com.florientmanfo.com.florientmanfo.data.table.Users
import com.florientmanfo.com.florientmanfo.models.user.UserModel
import com.florientmanfo.com.florientmanfo.utils.toLong
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class UsersEntity(id: EntityID<String>) : Entity<String>(id) {

    companion object : EntityClass<String, UsersEntity>(Users)

    var name by Users.name
    var email by Users.email
    var password by Users.password
    var activated by Users.activated
    var createdAt by Users.createdAt
    var updatedAt by Users.updatedAt

    fun toModel(): UserModel {
        return UserModel(
            name = name,
            email = email,
            activated = activated,
            createdAt = createdAt.toLong(),
            updatedAt = updatedAt.toLong()
        )
    }
}