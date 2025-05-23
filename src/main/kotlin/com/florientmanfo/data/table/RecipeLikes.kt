package com.florientmanfo.com.florientmanfo.data.tables

import com.florientmanfo.com.florientmanfo.data.table.Recipes
import com.florientmanfo.com.florientmanfo.data.table.Users
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object RecipeLikes: Table("recipe_likes") {
    val userId = varchar("user_id", 255).references(Users.id)
    val recipeId = varchar("recipe_id", 255).references(Recipes.id)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
    override val primaryKey = PrimaryKey(userId, recipeId)
}