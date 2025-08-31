package com.florientmanfo.com.florientmanfo.services

import com.florientmanfo.com.florientmanfo.services.recipe.RecipeService
import com.florientmanfo.com.florientmanfo.services.recipe.RecipeValidationService
import com.florientmanfo.com.florientmanfo.services.user.UserService
import com.florientmanfo.com.florientmanfo.services.user.UserValidationService
import org.koin.dsl.module

val serviceModule = module {
    single { UserValidationService() }
    single { UserService(get(), get(), get()) }
    single { RecipeValidationService() }
    single { RecipeService(get(), get()) }
}