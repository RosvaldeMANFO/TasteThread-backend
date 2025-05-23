package com.florientmanfo.com.florientmanfo.models.recipe

import kotlinx.serialization.Serializable

@Serializable
class IngredientModel(
    val id: String,
    val recipeId: String,
    val name: String,
    val quantity: Float,
    val unit: String?,
    val createdAt: Long,
    val updatedAt: Long,
)