package com.florientmanfo.com.florientmanfo.data.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.javatime.CurrentDateTime
import org.jetbrains.exposed.v1.javatime.datetime

object RecipeComments: IdTable<String>("recipe_comments") {
    override val id = varchar("id", 255).entityId()
    val recipeId = varchar("recipe_id", 255).references(Recipes.id, onDelete = ReferenceOption.CASCADE)
    val authorId = varchar("author_id", 255).references(Users.id, onDelete = ReferenceOption.CASCADE)
    val content = text("content")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    override val primaryKey = PrimaryKey(id)
}