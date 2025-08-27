package com.florientmanfo.com.florientmanfo.api.routing

import com.florientmanfo.api.module.webSocket
import com.florientmanfo.com.florientmanfo.services.recipe.RecipeService
import com.florientmanfo.com.florientmanfo.services.user.UserService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val userService: UserService by inject()
    val recipeService: RecipeService by inject()

    routing {
        authRouting(userService)
        userRouting(userService)
        authenticate("auth-jwt") {
            get("/") {
                call.respondText("Hello chef!")
            }
            protectedUserRouting(userService)
            protectedRecipeRouting(recipeService)
            webSocket()
        }
    }
}

fun retrieveAuthorId(call: ApplicationCall): String {
    val principal = call.principal<JWTPrincipal>()
    return principal?.payload?.getClaim("userId")?.asString() ?:
    throw IllegalArgumentException("Missing userId")
}