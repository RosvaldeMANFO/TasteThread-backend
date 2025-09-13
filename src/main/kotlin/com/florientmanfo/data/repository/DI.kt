package com.florientmanfo.com.florientmanfo.data.repository

import com.florientmanfo.com.florientmanfo.models.firebase.FirebaseRepository
import com.florientmanfo.com.florientmanfo.models.recipe.RecipeRepository
import com.florientmanfo.com.florientmanfo.models.user.UserRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<FirebaseRepository> { FirebaseRepositoryImpl(get()) }
    single<RecipeRepository> { RecipeRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get(), get()) }
}