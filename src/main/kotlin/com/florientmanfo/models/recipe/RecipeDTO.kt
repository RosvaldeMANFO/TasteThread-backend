package com.florientmanfo.com.florientmanfo.models.recipe

import kotlinx.serialization.Serializable

@Serializable
data class RecipeDTO(
    val name: String,
    val description: String,
    val imageUrl: String? = null,
    val ingredients: List<IngredientDTO>,
    val instructions: List<String>,
    val mealType: String,
    val dietaryRestrictions: List<String>,
    val origin: String,
    val cookTime: Int,
    val servings: Int,
)