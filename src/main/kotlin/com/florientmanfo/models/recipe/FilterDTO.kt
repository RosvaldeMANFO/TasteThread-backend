package com.florientmanfo.com.florientmanfo.models.recipe

import kotlinx.serialization.Serializable

@Serializable
data class FilterDTO(
    val query: String? = null,
    val origin: String? = null,
    val mealType: String? = null,
    val dietaryRestrictions: List<String> = listOf(),
    val mostLiked: Boolean? = null,
    val cookTime: Int? = null,
)