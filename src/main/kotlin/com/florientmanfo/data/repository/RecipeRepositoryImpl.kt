package com.florientmanfo.com.florientmanfo.data.repository

import com.florientmanfo.com.florientmanfo.data.entity.*
import com.florientmanfo.com.florientmanfo.data.table.RecipeLikes
import com.florientmanfo.com.florientmanfo.data.table.Recipes
import com.florientmanfo.com.florientmanfo.models.recipe.RecipeCommentDTO
import com.florientmanfo.com.florientmanfo.models.recipe.RecipeDTO
import com.florientmanfo.com.florientmanfo.models.recipe.RecipeModel
import com.florientmanfo.com.florientmanfo.models.recipe.RecipeRepository
import com.florientmanfo.com.florientmanfo.utils.IDGenerator
import com.florientmanfo.com.florientmanfo.utils.IDSuffix
import com.florientmanfo.com.florientmanfo.utils.suspendTransaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.or
import java.time.LocalDateTime

class RecipeRepositoryImpl : RecipeRepository {
    override suspend fun getAllRecipes(): Result<List<RecipeModel>> = suspendTransaction {
        try {
            val recipes = RecipesEntity.all().map { it.toModel() }
            Result.success(recipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createRecipe(authorId: String, recipe: RecipeDTO): Result<RecipeModel> = suspendTransaction {
        try {
            val newRecipe = RecipesEntity.new(IDGenerator.generate(IDSuffix.RECIPE)) {
                name = recipe.name
                description = recipe.description
                imageUrl = recipe.imageUrl
                instructions = recipe.instructions.joinToString("\n")
                this.authorId = authorId
                createdAt = LocalDateTime.now()
                updatedAt = LocalDateTime.now()
            }

            recipe.ingredients.forEach { ingredient ->
                IngredientsEntity.new(IDGenerator.generate(IDSuffix.INGREDIENT)) {
                    name = ingredient.name
                    quantity = ingredient.quantity
                    unit = ingredient.unit
                    recipeId = newRecipe.id.value
                    createdAt = LocalDateTime.now()
                    updatedAt = LocalDateTime.now()
                }
            }

            Result.success(newRecipe.toModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun deleteRecipe(authorId: String, id: String): Result<Unit> = suspendTransaction {
        try {
            val recipe =
                RecipesEntity.findById(id) ?: return@suspendTransaction Result.failure(Exception("Recipe not found"))

            if (recipe.author.id.value != authorId) {
                return@suspendTransaction Result.failure(Exception("You are not authorized to delete this recipe"))
            }

            recipe.delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun updateRecipe(recipeId: String, authorId: String, recipe: RecipeDTO): Result<RecipeModel> = suspendTransaction {
        try {
            val existingRecipe = RecipesEntity.findById(recipeId)
                ?: return@suspendTransaction Result.failure(Exception("Recipe not found"))

            if (existingRecipe.author.id.value != authorId) {
                return@suspendTransaction Result.failure(Exception("You are not authorized to update this recipe"))
            }

            existingRecipe.name = recipe.name
            existingRecipe.description = recipe.description
            existingRecipe.imageUrl = recipe.imageUrl
            existingRecipe.instructions = recipe.instructions.joinToString("\n")
            existingRecipe.updatedAt = LocalDateTime.now()

            existingRecipe.ingredients.forEach { it.delete() }
            recipe.ingredients.forEach { ingredient ->
                IngredientsEntity.new(IDGenerator.generate(IDSuffix.INGREDIENT)) {
                    name = ingredient.name
                    quantity = ingredient.quantity
                    unit = ingredient.unit
                    this.recipeId = recipeId
                    createdAt = LocalDateTime.now()
                    updatedAt = LocalDateTime.now()
                }
            }

            Result.success(existingRecipe.toModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun findRecipeByQuery(query: String): Result<List<RecipeModel>> =
        suspendTransaction {
            try {
                val recipes = RecipesEntity.find {
                    (Recipes.name.lowerCase() like "%${query.lowercase()}%") or
                            (Recipes.description.lowerCase() like "%${query.lowercase()}%")
                }.map { it.toModel() }

                Result.success(recipes)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }


    override suspend fun likeRecipe(userId: String, recipeId: String): Result<Unit> = suspendTransaction {
        try {
            val recipe = RecipesEntity.findById(recipeId)
                ?: return@suspendTransaction Result.failure(Exception("Recipe not found"))

            val existingLike =
                RecipeLikesEntity.find { (RecipeLikes.userId eq userId) and (RecipeLikes.recipeId eq recipeId) }
                    .firstOrNull()

            if (existingLike != null) {
                existingLike.delete()
            } else {
                RecipeLikesEntity.new {
                    this.userId = userId
                    this.recipeId = recipe.id.value
                    createdAt = LocalDateTime.now()
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun commentRecipe(userId: String, commentDTO: RecipeCommentDTO): Result<Unit> =
        suspendTransaction {
            try {
                RecipeCommentsEntity.new(IDGenerator.generate(IDSuffix.COMMENT)) {
                    authorId = userId
                    recipeId = commentDTO.recipeId
                    content = commentDTO.content
                    createdAt = LocalDateTime.now()
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getMyRecipes(userId: String): Result<List<RecipeModel>> {
        return try {
            suspendTransaction {
                RecipesEntity.find { Recipes.authorId eq userId }
                    .map { it.toModel() }
            }.let { recipes ->
                Result.success(recipes)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}