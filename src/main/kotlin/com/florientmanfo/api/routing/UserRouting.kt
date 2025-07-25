package com.florientmanfo.com.florientmanfo.api.routing

import com.florientmanfo.com.florientmanfo.models.user.LoginDTO
import com.florientmanfo.com.florientmanfo.models.user.RegisterDTO
import com.florientmanfo.com.florientmanfo.services.user.UserService
import com.florientmanfo.com.florientmanfo.utils.RequestResult
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*

fun Route.userRouting(service: UserService) {
    route("/users") {
        post("/register") {
            val dto = call.receive<RegisterDTO>()
            try {
                val result = service.register(dto)
                val response = result.fold(
                    onSuccess = { RequestResult.formatResult(result, HttpStatusCode.Created) },
                    onFailure = { RequestResult.formatResult(result, HttpStatusCode.InternalServerError) }
                )
                call.respond(HttpStatusCode.fromValue(response.httpStatus), response)
            } catch (e: Exception) {
                val result = Result.failure<String>(e)
                val response = RequestResult.formatResult(result, HttpStatusCode.BadRequest)
                call.respond(HttpStatusCode.fromValue(response.httpStatus), response)
            }
        }
        post("/login") {
            try {
                val dto = call.receive<LoginDTO>()
                val result = service.login(dto)
                val response = result.fold(
                    onSuccess = { RequestResult.formatResult(result, HttpStatusCode.OK) },
                    onFailure = { RequestResult.formatResult(result, HttpStatusCode.InternalServerError) }
                )
                call.respond(HttpStatusCode.fromValue(response.httpStatus), response)
            } catch (e: Exception) {
                val result = Result.failure<String>(e)
                val response = RequestResult.formatResult(result, HttpStatusCode.BadRequest)
                call.respond(HttpStatusCode.fromValue(response.httpStatus), response)
            }
        }
    }
}