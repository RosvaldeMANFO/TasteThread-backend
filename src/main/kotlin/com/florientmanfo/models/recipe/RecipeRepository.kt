package com.florientmanfo.com.florientmanfo.models.recipe

interface RecipeRepository {
    suspend fun getAllRecipes(limit: Int = 20, offset: Long = 0): Result<List<RecipeModel>>
    suspend fun createRecipe(authorId: String, dto: RecipeDTO, recipeImageFile: ByteArray? = null): Result<RecipeModel>
    suspend fun deleteRecipe(authorId: String, id: String): Result<Unit>
    suspend fun updateRecipe(recipeId: String, authorId: String, dto: RecipeDTO, recipeImageFile: ByteArray? = null): Result<RecipeModel>
    suspend fun findRecipe(filter: FilterDTO, limit: Int = 20, offset: Long = 0): Result<List<RecipeModel>>
    suspend fun likeRecipe(userId: String, recipeId: String): Result<Unit>
    suspend fun commentRecipe(userId: String, commentDTO: RecipeCommentDTO): Result<Unit>
    suspend fun getMyRecipes(userId: String, limit: Int = 20, offset: Long = 0): Result<List<RecipeModel>>
}