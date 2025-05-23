package com.florientmanfo.com.florientmanfo.data.repository

import com.florientmanfo.com.florientmanfo.models.recipe.RecipeRepository
import com.florientmanfo.com.florientmanfo.models.user.UserRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<RecipeRepository> { RecipeRepositoryImpl() }
}