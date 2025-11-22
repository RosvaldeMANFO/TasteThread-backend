package com.florientmanfo.models.recipe

import com.florientmanfo.models.user.UserModel
import kotlinx.serialization.Serializable

@Serializable
class RecipeModel(
    val id: String,
    val name: String,
    val author: UserModel,
    val imageUrl: String?,
    val course: String,
    val description: String,
    val dietaryRestrictions: List<String>,
    val origin: String,
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