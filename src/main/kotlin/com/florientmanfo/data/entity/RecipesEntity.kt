package com.florientmanfo.com.florientmanfo.data.entity

import com.florientmanfo.com.florientmanfo.data.table.Ingredients
import com.florientmanfo.com.florientmanfo.data.table.RecipeComments
import com.florientmanfo.com.florientmanfo.data.table.RecipeLikes
import com.florientmanfo.com.florientmanfo.data.table.Recipes
import com.florientmanfo.com.florientmanfo.models.recipe.DietaryRestriction
import com.florientmanfo.com.florientmanfo.models.recipe.MealType
import com.florientmanfo.com.florientmanfo.models.recipe.Origin
import com.florientmanfo.com.florientmanfo.models.recipe.RecipeModel
import com.florientmanfo.com.florientmanfo.utils.toLong
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class RecipesEntity(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, RecipesEntity>(Recipes)

    var name by Recipes.name
    var description by Recipes.description
    var imageUrl by Recipes.imageUrl
    var instructions by Recipes.instructions
    var mealType by Recipes.mealType
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
            mealType = MealType.fromDisplayName(mealType),
            description = description,
            dietaryRestrictions = dietaryRestriction.split(",").map { DietaryRestriction.fromDisplayName(it) },
            country = Origin.fromDisplayName(origin),
            cookTime = cookTime,
            servings = servings,
            ingredients = ingredients.map { it.toModel() },
            instructions = instructions.split("\n"),
            comments = comments.sortedByDescending { it.createdAt }.map { it.toModel() },
            likes = likes.map { it.toModel() },
            approved = approved,
            createdAt = createdAt.toLong(),
            updatedAt = updatedAt.toLong()
        )
    }
}