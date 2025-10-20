package com.florientmanfo.com.florientmanfo.api.routing

import com.florientmanfo.com.florientmanfo.services.admin.AdminService
import com.florientmanfo.com.florientmanfo.utils.RequestResult
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.adminRouting(service: AdminService) {
    route("/admin") {
        get("/stats"){
            try {
                val result = service.getStats()
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