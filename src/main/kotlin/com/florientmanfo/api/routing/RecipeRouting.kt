package com.florientmanfo.com.florientmanfo.api.routing

import com.florientmanfo.com.florientmanfo.models.recipe.RecipeCommentDTO
import com.florientmanfo.com.florientmanfo.models.recipe.RecipeDTO
import com.florientmanfo.com.florientmanfo.services.recipe.RecipeService
import com.florientmanfo.com.florientmanfo.utils.RequestResult
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.protectedRecipeRouting(service: RecipeService) {
    route("/recipes") {
        post {
            try {
                val dto = call.receive<RecipeDTO>()
                val authorId  = retrieveAuthorId(call)

                val result = service.createRecipe(authorId, dto)
                val response = RequestResult.formatResult(result, HttpStatusCode.Created)
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
                val response = RequestResult.formatResult(result, HttpStatusCode.NoContent)
                call.respond(response)
            } catch (e: Exception) {
                val result = Result.failure<String>(e)
                val response = RequestResult.formatResult(result, HttpStatusCode.BadRequest)
                call.respond(response)
            }
        }

        put("/{id}") {
            try {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing recipe ID")
                val dto = call.receive<RecipeDTO>()
                val authorId = retrieveAuthorId(call)
                val result = service.updateRecipe(id, authorId, dto)
                val response = RequestResult.formatResult(result, HttpStatusCode.OK)
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
                val response = RequestResult.formatResult(result, HttpStatusCode.OK)
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
                val response = RequestResult.formatResult(result, HttpStatusCode.Created)
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
                val response = RequestResult.formatResult(result, HttpStatusCode.OK)
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
                val response = RequestResult.formatResult(result, HttpStatusCode.OK)
                call.respond(response)
            } catch (e: Exception) {
                val result = Result.failure<List<String>>(e)
                val response = RequestResult.formatResult(result, HttpStatusCode.BadRequest)
                call.respond(response)
            }
        }

        get("/search") {
            try {
                val query = call.request.queryParameters["query"] ?: throw IllegalArgumentException("Missing query parameter")
                val result = service.findRecipeByQuery(query)
                val response = RequestResult.formatResult(result, HttpStatusCode.OK)
                call.respond(response)
            } catch (e: Exception) {
                val result = Result.failure<String>(e)
                val response = RequestResult.formatResult(result, HttpStatusCode.BadRequest)
                call.respond(response)
            }
        }
    }
}