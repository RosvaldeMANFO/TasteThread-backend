package com.florientmanfo.com.florientmanfo.models.recipe

interface RecipeRepository {
    suspend fun getAllRecipes(): Result<List<RecipeModel>>
    suspend fun createRecipe(authorId: String, recipe: RecipeDTO): Result<RecipeModel>
    suspend fun deleteRecipe(authorId: String, id: String): Result<Unit>
    suspend fun updateRecipe(recipeId: String, authorId: String, recipe: RecipeDTO): Result<RecipeModel>
    suspend fun findRecipeByQuery(query: String): Result<List<RecipeModel>>
    suspend fun likeRecipe(userId: String, recipeId: String): Result<Unit>
    suspend fun commentRecipe(userId: String, commentDTO: RecipeCommentDTO): Result<Unit>
    suspend fun getMyRecipes(userId: String): Result<List<RecipeModel>>
}