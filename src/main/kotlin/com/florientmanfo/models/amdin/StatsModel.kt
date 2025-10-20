package com.florientmanfo.com.florientmanfo.models.amdin

import kotlinx.serialization.Serializable

@Serializable
data class StatsModel(
    val userCount: Int,
    val recipeCount: Int,
    val pendingRecipeCount: Int,
)