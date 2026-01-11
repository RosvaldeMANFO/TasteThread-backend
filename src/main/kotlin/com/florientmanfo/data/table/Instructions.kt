package com.florientmanfo.data.table

import com.florientmanfo.data.table.Recipes
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.javatime.CurrentDateTime
import org.jetbrains.exposed.v1.javatime.datetime

object Instructions: IdTable<String>("cooking_instructions") {
    override val id = varchar("id", 255).entityId()
    val description = text("description")
    val recipeId = varchar("recipe_id", 255).references(Recipes.id, onDelete = ReferenceOption.CASCADE)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    init {
        uniqueIndex(recipeId, description)
    }
}