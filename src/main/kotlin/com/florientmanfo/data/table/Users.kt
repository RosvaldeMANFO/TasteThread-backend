package com.florientmanfo.com.florientmanfo.data.table

import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.javatime.CurrentDateTime
import org.jetbrains.exposed.v1.javatime.datetime

object Users: IdTable<String>("users") {
    override val id = varchar("id", 50).entityId()
    val name = varchar("name", 255)
    val email = varchar("email", 255).uniqueIndex()
    val password = varchar("password", 255)
    val activated = bool("activated").default(false)
    val role = varchar("role", 20).default("USER")
    val imageUrl = varchar("image_path", 512).nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
    override val primaryKey = PrimaryKey(id)
}