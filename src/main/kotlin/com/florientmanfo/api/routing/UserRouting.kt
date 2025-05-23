package com.florientmanfo.com.florientmanfo.api.routing

import com.florientmanfo.com.florientmanfo.models.user.RegisterDTO
import com.florientmanfo.com.florientmanfo.services.user.UserService
import com.florientmanfo.com.florientmanfo.utils.RequestResult
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRouting(service: UserService) {
    route("/user") {
        post("/register") {
            val dto = call.receive<RegisterDTO>()
            try {
                val result = service.register(dto)
                val response = RequestResult.formatResult(result, HttpStatusCode.Created)
                call.respond(response)
            } catch (e: Exception) {
                val result = Result.failure<String>(e)
                val response = RequestResult.formatResult(result, HttpStatusCode.BadRequest)
                call.respond(response)
            }
        }
    }
}