package com.florientmanfo.com.florientmanfo.models.amdin

interface AdminRepository {
    suspend fun getStats(): Result<StatsModel>
    suspend fun approuveRecipe(recipeId: String): Result<Unit>
    suspend fun deleteRecipes(recipeId: String): Result<Unit>
}