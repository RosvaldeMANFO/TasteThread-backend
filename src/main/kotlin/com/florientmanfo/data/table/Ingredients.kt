package com.florientmanfo.com.florientmanfo.data.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.javatime.CurrentDateTime
import org.jetbrains.exposed.v1.javatime.datetime


object Ingredients : IdTable<String>("ingredients") {
    override val id = varchar("id", 100).entityId()
    val recipeId = varchar("recipe_id", 255).references(Recipes.id, onDelete = ReferenceOption.CASCADE).index()
    val name = varchar("name", 255)
    val quantity = float("quantity")
    val unit = varchar("unit", 20).nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
    val isDeleted = bool("is_deleted").default(false)
    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex("idx_recipe_ingredient_unique", recipeId, name)
    }
}
