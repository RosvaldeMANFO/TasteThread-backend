package com.florientmanfo.com.florientmanfo.data.table

import com.florientmanfo.com.florientmanfo.utils.IDGenerator
import com.florientmanfo.com.florientmanfo.utils.IDSuffix
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime

object RecipeComments: IdTable<String>("recipe_comments") {
    override val id = varchar("id", 255).entityId()
    val recipeId = varchar("recipe_id", 255).references(Recipes.id, onDelete = ReferenceOption.CASCADE)
    val authorId = varchar("author_id", 255).references(Users.id, onDelete = ReferenceOption.CASCADE)
    val content = text("content")
    val createdAt = datetime("created_at")
    override val primaryKey = PrimaryKey(id)
}