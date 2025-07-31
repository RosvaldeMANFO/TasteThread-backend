package com.florientmanfo.com.florientmanfo.models.recipe

import com.florientmanfo.com.florientmanfo.models.user.UserModel
import kotlinx.serialization.Serializable

@Serializable
class RecipeModel(
    val id: String,
    val name: String,
    val author: UserModel,
    val imageUrl: String?,
    val mealType: MealType,
    val description: String,
    val dietaryRestrictions: List<DietaryRestriction>,
    val country: Origin,
    val cookTime: Int,
    val servings: Int,
    val ingredients: List<IngredientModel>,
    val instructions: List<String>,
    val comments: List<RecipeCommentModel>,
    val likes: List<RecipeLikeModel>,
    val approved: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)