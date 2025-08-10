package com.florientmanfo.com.florientmanfo.data.repository

import com.florientmanfo.com.florientmanfo.data.entity.IngredientsEntity
import com.florientmanfo.com.florientmanfo.data.entity.RecipeCommentsEntity
import com.florientmanfo.com.florientmanfo.data.entity.RecipeLikesEntity
import com.florientmanfo.com.florientmanfo.data.entity.RecipesEntity
import com.florientmanfo.com.florientmanfo.data.table.RecipeLikes
import com.florientmanfo.com.florientmanfo.data.table.Recipes
import com.florientmanfo.com.florientmanfo.models.firebase.FirebaseRepository
import com.florientmanfo.com.florientmanfo.models.recipe.*
import com.florientmanfo.com.florientmanfo.utils.IDGenerator
import com.florientmanfo.com.florientmanfo.utils.IDSuffix
import com.florientmanfo.com.florientmanfo.utils.suspendTransaction
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.or
import java.time.LocalDateTime

class RecipeRepositoryImpl(private val firebase: FirebaseRepository) : RecipeRepository {
    override suspend fun getAllRecipes(limit: Int, offset: Long): Result<List<RecipeModel>> = suspendTransaction {
        try {
            val recipes = RecipesEntity.all()
                .limit(limit).offset(offset)
                .map { it.toModel() }.filter { it.approved }
            Result.success(recipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecipe(id: String): Result<RecipeModel?> {
        return try {
            suspendTransaction {
                val recipe = RecipesEntity.findById(id)
                Result.success(recipe?.toModel())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createRecipe(
        authorId: String,
        dto: RecipeDTO,
        recipeImageFile: ByteArray?
    ): Result<RecipeModel> = suspendTransaction {

        try {
            val id = IDGenerator.generate(IDSuffix.RECIPE)
            val imageUrl = recipeImageFile?.let {
                firebase.uploadFile(it, id)
            }

            val newRecipe = RecipesEntity.new(id) {
                name = dto.name
                description = dto.description
                this.imageUrl = imageUrl
                instructions = dto.instructions.joinToString("\n")
                mealType = dto.mealType.toDisplayName()
                dietaryRestriction = dto.dietaryRestrictions.joinToString(",") { it.toDisplayName() }
                origin = dto.origin.toDisplayName()
                cookTime = dto.cookTime
                servings = dto.servings
                approved = true
                this.authorId = authorId
                createdAt = LocalDateTime.now()
                updatedAt = LocalDateTime.now()
            }

            dto.ingredients.forEach { ingredient ->
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

    override suspend fun updateRecipe(
        recipeId: String,
        authorId: String,
        dto: RecipeDTO,
        recipeImageFile: ByteArray?
    ): Result<RecipeModel> = suspendTransaction {
        try {
            val existingRecipe = RecipesEntity.findById(recipeId)
                ?: return@suspendTransaction Result.failure(Exception("Recipe not found"))

            if (existingRecipe.authorId != authorId) {
                return@suspendTransaction Result.failure(Exception("Unauthorized access"))
            }

            dto.imageUrl?.let {
                existingRecipe.imageUrl = it
            } ?: existingRecipe.imageUrl?.let {
                firebase.deleteFile(existingRecipe.id.value)
                existingRecipe.imageUrl = null
            }

            recipeImageFile?.let {
                existingRecipe.imageUrl = firebase.uploadFile(it, recipeId)
            }

            existingRecipe.name = dto.name
            existingRecipe.description = dto.description
            existingRecipe.instructions = dto.instructions.joinToString("\n")
            existingRecipe.updatedAt = LocalDateTime.now()
            existingRecipe.mealType = dto.mealType.toDisplayName()
            existingRecipe.dietaryRestriction = dto.dietaryRestrictions.joinToString(",") {
                it.toDisplayName()
            }
            existingRecipe.origin = dto.origin.toDisplayName()
            existingRecipe.cookTime = dto.cookTime
            existingRecipe.servings = dto.servings

            existingRecipe.ingredients.forEach { it.delete() }
            dto.ingredients.forEach { ingredient ->
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

    override suspend fun deleteRecipe(authorId: String, id: String): Result<Unit> = suspendTransaction {
        try {
            val recipe =
                RecipesEntity.findById(id) ?: return@suspendTransaction Result.failure(Exception("Recipe not found"))

            if (recipe.authorId != authorId) {
                return@suspendTransaction Result.failure(Exception("Unauthorized access"))
            }

            if (recipe.imageUrl != null) {
                firebase.deleteFile(recipe.id.value)
            }

            recipe.delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun findRecipe(filter: FilterDTO, limit: Int, offset: Long): Result<List<RecipeModel>> =
        suspendTransaction {
            try {
                val conditions = mutableListOf<Op<Boolean>>()

                filter.query?.let { query ->
                    conditions += (Recipes.name.lowerCase() like "%${query.lowercase()}%") or
                            (Recipes.description.lowerCase() like "%${query.lowercase()}%")
                }

                filter.origin?.let { origin ->
                    conditions += Recipes.origin eq origin
                }

                filter.mealType?.let { mealType ->
                    conditions += Recipes.mealType eq mealType
                }

                if (filter.dietaryRestrictions.isNotEmpty()) {
                    conditions += Recipes.dietaryRestriction.lowerCase() like
                            "%${filter.dietaryRestrictions.joinToString(",").lowercase()}%"
                }

                filter.cookTime?.let { cookTime ->
                    conditions += Recipes.cookTime lessEq cookTime
                }

                val queryOp = conditions.reduceOrNull { acc, op -> acc and op } ?: Op.TRUE

                val recipes = RecipesEntity.find { queryOp }
                    .limit(limit).offset(offset)
                    .toList()

                val sortedRecipes = when (filter.mostLiked) {
                    true -> recipes.sortedByDescending { it.likes.count() }
                    false -> recipes.sortedBy { it.likes.count() }
                    null -> recipes.sortedByDescending { it.createdAt }
                }

                Result.success(sortedRecipes.map { it.toModel() })
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

    override suspend fun getMyRecipes(userId: String, limit: Int, offset: Long): Result<List<RecipeModel>> {
        return try {
            suspendTransaction {
                RecipesEntity.find { Recipes.authorId eq userId }
                    .limit(limit).offset(offset)
                    .map { it.toModel() }
            }.let { recipes ->
                Result.success(recipes)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}