package com.florientmanfo.com.florientmanfo.data.entity

import com.florientmanfo.com.florientmanfo.data.table.Ingredients
import com.florientmanfo.com.florientmanfo.data.table.RecipeComments
import com.florientmanfo.com.florientmanfo.data.table.RecipeLikes
import com.florientmanfo.com.florientmanfo.data.table.Recipes
import com.florientmanfo.com.florientmanfo.models.recipe.RecipeModel
import com.florientmanfo.com.florientmanfo.utils.toLong
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class RecipesEntity(id: EntityID<String>): Entity<String>(id) {
    companion object : EntityClass<String, RecipesEntity>(Recipes)

    var name by Recipes.name
    var description by Recipes.description
    var imageUrl by Recipes.imageUrl
    var instructions by Recipes.instructions
    val author by UsersEntity referencedOn Recipes.authorId
    var authorId by Recipes.authorId
    var createdAt by Recipes.createdAt
    var updatedAt by Recipes.updatedAt
    val ingredients by IngredientsEntity.referrersOn(Ingredients.recipeId, true)
    private val comments by RecipeCommentsEntity.referrersOn(RecipeComments.recipeId, true)
    private val likes by RecipeLikesEntity.referrersOn(RecipeLikes.recipeId, true)

    fun toModel(): RecipeModel {
        return RecipeModel(
            id.value,
            name,
            author.toModel(),
            imageUrl,
            description,
            ingredients.map { it.toModel() },
            instructions.split("\n"),
            comments.map { it.toModel() },
            likes.map { it.toModel() },
            createdAt.toLong(),
            updatedAt.toLong()
        )
    }
}