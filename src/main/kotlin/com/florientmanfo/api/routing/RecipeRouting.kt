package com.florientmanfo.com.florientmanfo.api.routing

import com.florientmanfo.com.florientmanfo.models.recipe.RecipeCommentDTO
import com.florientmanfo.com.florientmanfo.models.recipe.RecipeDTO
import com.florientmanfo.com.florientmanfo.services.recipe.RecipeService
import com.florientmanfo.com.florientmanfo.utils.RequestResult
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
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

    dto?.let {
        return callBack(it, recipeImageFile)
    } ?: throw IllegalArgumentException("Missing recipe data")
}

fun Route.protectedRecipeRouting(service: RecipeService) {
    route("/recipes") {
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
                call.respond(response)
            } catch (e: Exception) {
                e.printStackTrace()
                val result = Result.failure<String>(e)
                val response = RequestResult.formatResult(result, HttpStatusCode.BadRequest)
                call.respond(response)
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
                call.respond(response)
            } catch (e: Exception) {
                val result = Result.failure<String>(e)
                val response = RequestResult.formatResult(result, HttpStatusCode.BadRequest)
                call.respond(response)
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
                call.respond(response)
            } catch (e: Exception) {
                val result = Result.failure<String>(e)
                val response = RequestResult.formatResult(result, HttpStatusCode.BadRequest)
                call.respond(response)
            }
        }

        post("/{id}/like") {
            try {
                val recipeId = call.parameters["id"] ?: throw IllegalArgumentException("Missing recipe ID")
                val userId = retrieveAuthorId(call)
                val result = service.likeRecipe(userId, recipeId)
                val response = result.fold(
                    onSuccess = { RequestResult.formatResult(result, HttpStatusCode.Created) },
                    onFailure = { RequestResult.formatResult(result, HttpStatusCode.InternalServerError) }
                )
                call.respond(response)
            } catch (e: Exception) {
                val result = Result.failure<String>(e)
                val response = RequestResult.formatResult(result, HttpStatusCode.BadRequest)
                call.respond(response)
            }
        }

        post("/{id}/comments") {
            try {
                val commentDTO = call.receive<RecipeCommentDTO>()
                val userId = retrieveAuthorId(call)
                val result = service.commentRecipe(userId, commentDTO)
                val response = result.fold(
                    onSuccess = { RequestResult.formatResult(result, HttpStatusCode.Created) },
                    onFailure = { RequestResult.formatResult(result, HttpStatusCode.InternalServerError) }
                )
                call.respond(response)
            } catch (e: Exception) {
                val result = Result.failure<String>(e)
                val response = RequestResult.formatResult(result, HttpStatusCode.BadRequest)
                call.respond(response)
            }
        }

        get("/my") {
            try {
                val userId = retrieveAuthorId(call)
                val result = service.getMyRecipes(userId)
                val response = result.fold(
                    onSuccess = { RequestResult.formatResult(result, HttpStatusCode.OK) },
                    onFailure = { RequestResult.formatResult(result, HttpStatusCode.InternalServerError) }
                )
                call.respond(response)
            } catch (e: Exception) {
                val result = Result.failure<String>(e)
                val response = RequestResult.formatResult(result, HttpStatusCode.BadRequest)
                call.respond(response)
            }
        }
    }
}

fun Route.recipeRouting(service: RecipeService) {
    route("/recipes") {
        get {
            try {
                val result = service.getAllRecipes()
                val response = result.fold(
                    onSuccess = { RequestResult.formatResult(result, HttpStatusCode.OK) },
                    onFailure = { RequestResult.formatResult(result, HttpStatusCode.InternalServerError) }
                )
                call.respond(response)
            } catch (e: Exception) {
                val result = Result.failure<List<String>>(e)
                val response = RequestResult.formatResult(result, HttpStatusCode.BadRequest)
                call.respond(response)
            }
        }

        get("/search") {
            try {
                val query = call.request.queryParameters["query"]
                    ?: throw IllegalArgumentException("Missing query parameter")
                val result = service.findRecipeByQuery(query)
                val response = result.fold(
                    onSuccess = { RequestResult.formatResult(result, HttpStatusCode.OK) },
                    onFailure = { RequestResult.formatResult(result, HttpStatusCode.InternalServerError) }
                )
                call.respond(response)
            } catch (e: Exception) {
                val result = Result.failure<String>(e)
                val response = RequestResult.formatResult(result, HttpStatusCode.BadRequest)
                call.respond(response)
            }
        }
    }
}