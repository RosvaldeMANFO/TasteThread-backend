package com.florientmanfo.com.florientmanfo.api.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.options

fun Route.preflightedRoute() {
    options("/users/activate") { call.respond(HttpStatusCode.NoContent) }
    options("/users/reset-password") { call.respond(HttpStatusCode.NoContent) }
}