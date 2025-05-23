package com.florientmanfo.com.florientmanfo.models.recipe

import kotlinx.serialization.Serializable

@Serializable
data class IngredientDTO(
    val id: String? = null,
    val name: String,
    val quantity: Float,
    val unit: String,
)