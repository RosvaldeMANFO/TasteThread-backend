package com.florientmanfo.com.florientmanfo.services.admin

import com.florientmanfo.com.florientmanfo.models.amdin.AdminRepository
import com.florientmanfo.com.florientmanfo.models.amdin.StatsModel
import com.florientmanfo.com.florientmanfo.models.recipe.FilterDTO
import com.florientmanfo.com.florientmanfo.models.recipe.RecipeModel
import com.florientmanfo.com.florientmanfo.models.recipe.RecipeRepository

class AdminService(
    private val adminRepository: AdminRepository,
    private val recipeRepository: RecipeRepository
) {
    suspend fun getStats(): Result<StatsModel>{
        return adminRepository.getStats()
    }

    suspend fun approveRecipe(recipeId: String): Result<Unit>{
        return adminRepository.approuveRecipe(recipeId)
    }

    suspend fun getRecipes(limit: Int, offset: Long, pending: Boolean):  Result<List<RecipeModel>>{
        return  recipeRepository.getAllRecipes(limit, offset).map {
            if (pending) {
                it.filter { recipe -> !recipe.approved }
            } else {
                it
            }
        }
    }

    suspend fun findRecipes(filter: FilterDTO, limit: Int, offset: Long, pending: Boolean):  Result<List<RecipeModel>>{
        return  recipeRepository.findRecipe(filter, limit, offset).map {
            if (pending) {
                it.filter { recipe -> !recipe.approved }
            } else {
                it
            }
        }
    }
}