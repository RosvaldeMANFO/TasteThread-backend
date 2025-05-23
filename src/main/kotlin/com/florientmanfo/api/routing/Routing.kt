package com.florientmanfo.com.florientmanfo.api.routing

import com.florientmanfo.com.florientmanfo.services.user.UserService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val userService: UserService by inject()

    routing {
        authRouting(userService)
        userRouting(userService)
        authenticate("auth-jwt") {
            get("/") {
                call.respondText("Hello cooker!")
            }
        }
    }
}
