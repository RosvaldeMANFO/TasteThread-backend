package com.florientmanfo.com.florientmanfo.services.recipe

import com.florientmanfo.com.florientmanfo.models.recipe.*

class RecipeService(
    private val repository: RecipeRepository,
    private val validation: RecipeValidationService
) {
    suspend fun getAllRecipes(): Result<List<RecipeModel>> {
        return repository.getAllRecipes()
    }

    suspend fun createRecipe(authorId: String, recipe: RecipeDTO): Result<RecipeModel> {
        val result = validation.validateRecipe(recipe)
        if (!result.isValid) {
            throw Exception(result.message)
        }
        return repository.createRecipe(authorId, recipe)
    }

    suspend fun deleteRecipe(authorId: String, id: String): Result<Unit> {
        return repository.deleteRecipe(authorId, id)
    }

    suspend fun updateRecipe(recipeId: String, authorId: String, recipe: RecipeDTO): Result<RecipeModel> {
        val result = validation.validateRecipe(recipe)
        if (!result.isValid) {
            throw Exception(result.message)
        }
        return repository.updateRecipe(recipeId, authorId, recipe)
    }

    suspend fun findRecipeByQuery(query: String): Result<List<RecipeModel>> {
        if (query.isBlank()) {
            throw Exception("The query cannot be empty.")
        }
        return repository.findRecipeByQuery(query)
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

    suspend fun getMyRecipes(userId: String): Result<List<RecipeModel>> {
        return repository.getMyRecipes(userId)
    }
}