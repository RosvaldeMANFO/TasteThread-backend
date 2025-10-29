package com.florientmanfo.com.florientmanfo.api.routing

import com.florientmanfo.com.florientmanfo.models.recipe.FilterDTO
import com.florientmanfo.com.florientmanfo.services.admin.AdminService
import com.florientmanfo.com.florientmanfo.utils.RequestResult
import io.ktor.http.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.response.respond
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
                handleException(e)
            }
        }
        post("/approve/{id}") {
            try {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing recipe ID")
                val result = service.approveRecipe(id)
                val response = result.fold(
                    onSuccess = { RequestResult.formatResult(result, HttpStatusCode.OK) },
                    onFailure = { RequestResult.formatResult(result, HttpStatusCode.InternalServerError) }
                )
                call.respond(HttpStatusCode.fromValue(response.httpStatus), response)
            } catch (e: Exception){
                handleException(e)
            }
        }

        get("/recipes") {
            try {
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
                val offset = call.request.queryParameters["offset"]?.toLongOrNull() ?:0
                val pending = call.request.queryParameters["pending"]?.toBoolean() ?: false
                val result = service.getRecipes(limit, offset, pending)
                val response = result.fold(
                    onSuccess = { RequestResult.formatResult(result, HttpStatusCode.OK) },
                    onFailure = { RequestResult.formatResult(result, HttpStatusCode.InternalServerError) }
                )
                call.respond(HttpStatusCode.fromValue(response.httpStatus), response)
            } catch (e: Exception) {
                val result = Result.failure<List<String>>(e)
                val response = RequestResult.formatResult(result, HttpStatusCode.BadRequest)
                call.respond(HttpStatusCode.fromValue(response.httpStatus), response)
            }
        }
        post("/search") {
            try {
                val query = call.receive<FilterDTO>()
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
                val offset = call.request.queryParameters["offset"]?.toLongOrNull() ?:0
                val pending = call.request.queryParameters["pending"]?.toBoolean() ?: false
                val result = service.findRecipes(query, limit, offset, pending)
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

private suspend fun RoutingContext.handleException(e: Exception) {
    val result = Result.failure<String>(e)
    val response = RequestResult.formatResult(result, HttpStatusCode.BadRequest)
    call.respond(HttpStatusCode.fromValue(response.httpStatus), response)
}