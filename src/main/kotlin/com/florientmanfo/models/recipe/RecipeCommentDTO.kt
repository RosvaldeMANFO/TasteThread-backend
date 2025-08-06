package com.florientmanfo.com.florientmanfo.models.recipe

import kotlinx.serialization.Serializable

@Serializable
data class RecipeCommentDTO(
    val recipeId: String,
    val content: String,
)
