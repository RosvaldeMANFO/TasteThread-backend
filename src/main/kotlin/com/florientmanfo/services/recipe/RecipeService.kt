package com.florientmanfo.com.florientmanfo.services.recipe

import com.florientmanfo.com.florientmanfo.models.recipe.*
import com.florientmanfo.com.florientmanfo.models.user.UserRole

class RecipeService(
    private val repository: RecipeRepository,
    private val validation: RecipeValidationService
) {
    suspend fun getAllRecipes(limit: Int, offset: Long): Result<List<RecipeModel>> {
        return repository.getAllRecipes(limit, offset).map {
            it.filter { recipe -> recipe.approved }
        }
    }

    suspend fun getRecipe(id: String): Result<RecipeModel?> {
        return repository.getRecipe(id)
    }

    suspend fun createRecipe(authorId: String, recipe: RecipeDTO, image: ByteArray? = null): Result<RecipeModel> {
        val result = validation.validateRecipe(recipe)
        if (!result.isValid) {
            throw Exception(result.message)
        }
        return repository.createRecipe(authorId, recipe, image)
    }

    suspend fun updateRecipe(recipeId: String, authorId: String, recipe: RecipeDTO, image: ByteArray? = null): Result<RecipeModel> {
        val result = validation.validateRecipe(recipe)
        if (!result.isValid) {
            throw Exception(result.message)
        }
        return repository.updateRecipe(recipeId, authorId, recipe, image)
    }

    suspend fun deleteRecipe(authorId: String, id: String): Result<Unit> {
        return repository.deleteRecipe(authorId, id)
    }

    suspend fun findRecipes(filter: FilterDTO, limit: Int, offset: Long): Result<List<RecipeModel>> {
        return repository.findRecipe(filter, limit, offset).map {
            it.filter { recipe -> recipe.approved }
        }
    }

    suspend fun likeRecipe(userId: String, recipeId: String): Result<Unit> {
        return repository.likeRecipe(userId, recipeId)
    }

    suspend fun commentRecipe(userId: String, commentDTO: RecipeCommentDTO): Result<Unit> {
        val result = validation.validateComment(commentDTO)
        if (!result.isValid) {
            throw Exception(result.message)
        }
        return repository.commentRecipe(userId, commentDTO)
    }

    suspend fun getMyRecipes(userId: String, limit: Int, offset: Long): Result<List<RecipeModel>> {
        return repository.getMyRecipes(userId, limit, offset)
    }
}