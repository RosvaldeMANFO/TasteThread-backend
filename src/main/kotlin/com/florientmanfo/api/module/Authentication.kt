package com.florientmanfo.com.florientmanfo.api.module

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureAuthentication() {
    val secret = this.environment.config.property("ktor.jwt.secret").getString()
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT
                .require(Algorithm.HMAC256(secret))
                .build()
            )
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asString()
                if (!userId.isNullOrEmpty()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}