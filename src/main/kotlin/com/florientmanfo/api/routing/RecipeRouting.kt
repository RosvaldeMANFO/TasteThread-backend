package com.florientmanfo.com.florientmanfo.api.routing

import com.florientmanfo.api.module.SocketMessage
import com.florientmanfo.api.module.notifyAllClients
import com.florientmanfo.com.florientmanfo.models.recipe.FilterDTO
import com.florientmanfo.com.florientmanfo.models.recipe.RecipeCommentDTO
import com.florientmanfo.com.florientmanfo.models.recipe.RecipeDTO
import com.florientmanfo.com.florientmanfo.services.recipe.RecipeService
import com.florientmanfo.com.florientmanfo.utils.RequestResult
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

private suspend fun <T> multipartRecipe(
    call: ApplicationCall,
    callBack: suspend (RecipeDTO, ByteArray?) -> RequestResult<T>
): RequestResult<T> {
    val multiPartData = call.receiveMultipart()
    var recipeImageFile: ByteArray? = null
    var dto: RecipeDTO? = null

    multiPartData.forEachPart { part ->
        when (part) {
            is PartData.FormItem -> {
                if (part.name == "recipe") {
                    dto = Json.decodeFromString<RecipeDTO>(part.value)
                }
            }

            is PartData.FileItem -> {
                if (part.name == "image") {
                    recipeImageFile = part.streamProvider().readBytes()
                }
            }

            else -> Unit
        }
        part.dispose()
    }

    return dto?.let {
        callBack(it, recipeImageFile)
    } ?: throw IllegalArgumentException("Missing recipe data")
}

fun Route.protectedRecipeRouting(service: RecipeService) {
    route("/recipes") {
        get {
            try {
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
                val offset = call.request.queryParameters["offset"]?.toLongOrNull() ?:0
                val result = service.getAllRecipes(limit, offset)
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
                val result = service.findRecipe(query, limit, offset)
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
        post {
            try {
                val authorId = retrieveAuthorId(call)
                val response = if (call.request.isMultipart()) {
                    multipartRecipe(call) { dto, image ->
                        val result = service.createRecipe(authorId, dto, image)
                        result.fold(
                            onSuccess = { RequestResult.formatResult(result, HttpStatusCode.Created) },
                            onFailure = { RequestResult.formatResult(result, HttpStatusCode.InternalServerError) }
                        )
                    }
                } else {
                    val dto = call.receive<RecipeDTO>()
                    val result = service.createRecipe(authorId, dto)
                    result.fold(
                        onSuccess = { RequestResult.formatResult(result, HttpStatusCode.Created) },
                        onFailure = { RequestResult.formatResult(result, HttpStatusCode.InternalServerError) }
                    )
                }
                notifyAllClients(SocketMessage.RECIPE_CREATED)
                call.respond(HttpStatusCode.fromValue(response.httpStatus), response)
            } catch (e: Exception) {
                e.printStackTrace()
                val result = Result.failure<String>(e)
                val response = RequestResult.formatResult(result, HttpStatusCode.BadRequest)
                call.respond(HttpStatusCode.fromValue(response.httpStatus), response)
            }
        }

        put("/{id}") {
            try {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing recipe ID")
                val authorId = retrieveAuthorId(call)
                val response = if (call.request.isMultipart()) {
                    multipartRecipe(call) { dto, image ->
                        val result = service.updateRecipe(id, authorId, dto, image)
                        result.fold(
                            onSuccess = { RequestResult.formatResult(result, HttpStatusCode.Created) },
                            onFailure = { RequestResult.formatResult(result, HttpStatusCode.InternalServerError) }
                        )
                    }
                } else {
                    val dto = call.receive<RecipeDTO>()
                    val result = service.updateRecipe(id, authorId, dto)
                    result.fold(
                        onSuccess = { RequestResult.formatResult(result, HttpStatusCode.OK) },
                        onFailure = { RequestResult.formatResult(result, HttpStatusCode.InternalServerError) }
                    )
                }
                notifyAllClients(SocketMessage.RECIPE_UPDATED)
                call.respond(HttpStatusCode.fromValue(response.httpStatus), response)
            } catch (e: Exception) {
                val result = Result.failure<String>(e)
                val response = RequestResult.formatResult(result, HttpStatusCode.BadRequest)
                call.respond(HttpStatusCode.fromValue(response.httpStatus), response)
            }
        }

        delete("/{id}") {
            try {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing recipe ID")
                val authorId = retrieveAuthorId(call)
                val result = service.deleteRecipe(authorId, id)
                val response = result.fold(
                    onSuccess = { RequestResult.formatResult(result, HttpStatusCode.OK) },
                    onFailure = { RequestResult.formatResult(result, HttpStatusCode.InternalServerError) }
                )
                notifyAllClients(SocketMessage.RECIPE_DELETED)
                call.respond(HttpStatusCode.fromValue(response.httpStatus), response)
            } catch (e: Exception) {
                val result = Result.failure<String>(e)
                val response = RequestResult.formatResult(result, HttpStatusCode.BadRequest)
                call.respond(HttpStatusCode.fromValue(response.httpStatus), response)
            }
        }

        post("/like") {
            try {
                val recipeId = call.receive<String>()
                val userId = retrieveAuthorId(call)
                val result = service.likeRecipe(userId, recipeId)
                val response = result.fold(
                    onSuccess = { RequestResult.formatResult(result, HttpStatusCode.Created) },
                    onFailure = { RequestResult.formatResult(result, HttpStatusCode.InternalServerError) }
                )
                notifyAllClients(SocketMessage.RECIPE_LIKED)
                call.respond(HttpStatusCode.fromValue(response.httpStatus), response)
            } catch (e: Exception) {
                val result = Result.failure<String>(e)
                val response = RequestResult.formatResult(result, HttpStatusCode.BadRequest)
                call.respond(HttpStatusCode.fromValue(response.httpStatus), response)
            }
        }

        post("/comments") {
            try {
                val commentDTO = call.receive<RecipeCommentDTO>()
                val userId = retrieveAuthorId(call)
                val result = service.commentRecipe(userId, commentDTO)
                val response = result.fold(
                    onSuccess = { RequestResult.formatResult(result, HttpStatusCode.Created) },
                    onFailure = { RequestResult.formatResult(result, HttpStatusCode.InternalServerError) }
                )
                notifyAllClients(SocketMessage.RECIPE_COMMENTED)
                call.respond(HttpStatusCode.fromValue(response.httpStatus), response)
            } catch (e: Exception) {
                val result = Result.failure<String>(e)
                val response = RequestResult.formatResult(result, HttpStatusCode.BadRequest)
                call.respond(HttpStatusCode.fromValue(response.httpStatus), response)
            }
        }

        get("/my") {
            try {
                val userId = retrieveAuthorId(call)

                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
                val offset = call.request.queryParameters["offset"]?.toLongOrNull() ?:0
                val result = service.getMyRecipes(userId, limit, offset)
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