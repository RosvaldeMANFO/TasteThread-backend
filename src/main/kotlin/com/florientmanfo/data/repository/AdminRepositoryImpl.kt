package com.florientmanfo.com.florientmanfo.data.repository

import com.florientmanfo.com.florientmanfo.data.entity.RecipesEntity
import com.florientmanfo.com.florientmanfo.data.entity.UsersEntity
import com.florientmanfo.com.florientmanfo.data.table.Recipes
import com.florientmanfo.com.florientmanfo.data.table.Users
import com.florientmanfo.com.florientmanfo.models.amdin.AdminRepository
import com.florientmanfo.com.florientmanfo.models.amdin.StatsModel
import com.florientmanfo.com.florientmanfo.models.firebase.FirebaseRepository
import com.florientmanfo.com.florientmanfo.models.recipe.RecipeRepository
import com.florientmanfo.com.florientmanfo.models.user.UserRole
import com.florientmanfo.com.florientmanfo.utils.RequestResult
import com.florientmanfo.com.florientmanfo.utils.suspendTransaction
import io.ktor.server.config.ApplicationConfig
import org.jetbrains.exposed.v1.core.and

class AdminRepositoryImpl(
    private val config: ApplicationConfig,
    private val firebase: FirebaseRepository,
) : AdminRepository {

    override suspend fun getStats(): Result<StatsModel> {
        return suspendTransaction {
            val recipeCount = RecipesEntity.find {
                Recipes.approved eq true
            }.count().toInt()
            val userCount = UsersEntity.find {
                (Users.role neq  UserRole.ADMIN.name) and (Users.activated eq true)
            }.count().toInt()
            val pendingRecipeCount = RecipesEntity.find {
                Recipes.approved eq false
            }.count().toInt()
            Result.success(StatsModel(userCount, recipeCount, pendingRecipeCount))
        }
    }

    override suspend fun deleteUserAccount(userEmail: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteRecipes(recipeId: String): Result<Unit> {
        TODO("Not yet implemented")
    }

}