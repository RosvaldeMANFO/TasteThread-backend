package com.florientmanfo.com.florientmanfo.api.routing

import com.florientmanfo.com.florientmanfo.models.user.LoginDTO
import com.florientmanfo.com.florientmanfo.services.user.UserService
import com.florientmanfo.com.florientmanfo.utils.RequestResult
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRouting(service: UserService) {
    route("/login"){
        post {
            val response = try {
                val dto = call.receive<LoginDTO>()
                val result = service.login(dto)
                RequestResult.formatResult(result, HttpStatusCode.OK)
            } catch (e: Exception) {
                val result = Result.failure<String>(e)
                RequestResult.formatResult(result, HttpStatusCode.BadRequest)
            }
            call.respond(response)
        }
    }
}