package com.florientmanfo.com.florientmanfo.data.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.javatime.CurrentDateTime
import org.jetbrains.exposed.v1.javatime.datetime

object RecipeLikes: IntIdTable("recipe_likes") {
    val userId = varchar("user_id", 255).references(Users.id, onDelete = ReferenceOption.CASCADE)
    val recipeId = varchar("recipe_id", 255).references(Recipes.id, onDelete = ReferenceOption.CASCADE)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}