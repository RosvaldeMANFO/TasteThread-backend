package com.florientmanfo.data.entity

import com.florientmanfo.data.table.Instructions
import com.florientmanfo.data.table.Ingredients
import com.florientmanfo.data.table.RecipeComments
import com.florientmanfo.data.table.RecipeLikes
import com.florientmanfo.data.table.Recipes
import com.florientmanfo.models.recipe.RecipeModel
import com.florientmanfo.utils.toLong
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

class RecipesEntity(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, RecipesEntity>(Recipes)

    var name by Recipes.name
    var description by Recipes.description
    var imageUrl by Recipes.imageUrl
    val instructions by InstructionsEntity.referrersOn(Instructions.recipeId, true)
    var course by Recipes.course
    var dietaryRestriction by Recipes.dietaryRestriction
    var origin by Recipes.origin
    var cookTime by Recipes.cookTime
    var servings by Recipes.servings
    var approved by Recipes.approved
    val author by UsersEntity referencedOn Recipes.authorId
    var authorId by Recipes.authorId
    var createdAt by Recipes.createdAt
    var updatedAt by Recipes.updatedAt
    val ingredients by IngredientsEntity.referrersOn(Ingredients.recipeId, true)
    private val comments by RecipeCommentsEntity.referrersOn(RecipeComments.recipeId, true)
    val likes by RecipeLikesEntity.referrersOn(RecipeLikes.recipeId, true)

    fun toModel(): RecipeModel {
        return RecipeModel(
            id = id.value,
            name = name,
            author = author.toModel(),
            imageUrl = imageUrl,
            course = course,
            description = description,
            dietaryRestrictions = dietaryRestriction.split(","),
            origin = origin,
            cookTime = cookTime,
            servings = servings,
            ingredients = ingredients.map { it.toModel() },
            instructions = instructions.map { it.description },
            comments = comments.sortedByDescending { it.createdAt }.map { it.toModel() },
            likes = likes.map { it.toModel() },
            approved = approved,
            createdAt = createdAt.toLong(),
            updatedAt = updatedAt.toLong()
        )
    }
}