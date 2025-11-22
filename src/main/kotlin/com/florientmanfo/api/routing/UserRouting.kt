package com.florientmanfo.api.routing

import com.florientmanfo.api.module.SocketMessage
import com.florientmanfo.api.module.notifyAllClients
import com.florientmanfo.models.user.RegisterDTO
import com.florientmanfo.models.user.UserDTO
import com.florientmanfo.services.user.UserService
import com.florientmanfo.utils.RequestResult
import com.florientmanfo.utils.handleException
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

private suspend fun <T> multipart(
    call: ApplicationCall,
    callBack: suspend (UserDTO, ByteArray?) -> RequestResult<T>
): RequestResult<T> {
    val multiPartData = call.receiveMultipart()
    var recipeImageFile: ByteArray? = null
    var dto: UserDTO? = null

    multiPartData.forEachPart { part ->
        when (part) {
            is PartData.FormItem -> {
                if (part.name == "dto") {
                    val json = Json {
                        ignoreUnknownKeys = true
                    }
                    dto = json.decodeFromString<UserDTO>(part.value)
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

fun Route.userRouting(service: UserService) {
    route("/users") {
        post("/register") {
            val dto = call.receive<RegisterDTO>()
            try {
                val result = service.register(dto)
                val response = result.fold(
                    onSuccess = { RequestResult.formatResult(result, HttpStatusCode.Created) },
                    onFailure = { RequestResult.formatResult(result, HttpStatusCode.BadRequest) }
                )
                call.respond(HttpStatusCode.fromValue(response.httpStatus), response)
            } catch (e: Exception) {
                handleException(e)
            }
        }

        post("/request-password-reset") {
            val email = call.receive<String>()
            try {
                val result = service.requestPasswordReset(email)
                val response = result.fold(
                    onSuccess = { RequestResult.formatResult(result, HttpStatusCode.OK) },
                    onFailure = { RequestResult.formatResult(result, HttpStatusCode.BadRequest) }
                )
                call.respond(HttpStatusCode.fromValue(response.httpStatus), response)
            } catch (e: Exception) {
                handleException(e)
            }
        }

        post("/request-account-activation") {
            val email = call.receive<String>()
            try {
                val result = service.requestAccountActivation(email)
                val response = result.fold(
                    onSuccess = { RequestResult.formatResult(result, HttpStatusCode.OK) },
                    onFailure = { RequestResult.formatResult(result, HttpStatusCode.BadRequest) }
                )
                call.respond(HttpStatusCode.fromValue(response.httpStatus), response)
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }
}

fun Route.protectedUserRouting(service: UserService) {
    route("/users") {
        get("/profile") {
            val userId = retrieveUserId(call)
            val result = service.getProfile(userId)
            val response = result.fold(
                onSuccess = { RequestResult.formatResult(result, HttpStatusCode.OK) },
                onFailure = { RequestResult.formatResult(result, HttpStatusCode.BadRequest) }
            )
            call.respond(HttpStatusCode.fromValue(response.httpStatus), response)
        }

        post("/activate") {
            val userId = retrieveUserId(call)
            try {
                val result = service.activateAccount(userId)
                val response = result.fold(
                    onSuccess = { RequestResult.formatResult(result, HttpStatusCode.OK) },
                    onFailure = { RequestResult.formatResult(result, HttpStatusCode.BadRequest) }
                )
                call.respond(HttpStatusCode.fromValue(response.httpStatus), response)
            } catch (e: Exception) {
                handleException(e)
            }
        }

        post("/reset-password") {
            val userId = retrieveUserId(call)
            val newPassword = call.receive<String>()
            try {
                val result = service.resetPassword(userId, newPassword)
                val response = result.fold(
                    onSuccess = { RequestResult.formatResult(result, HttpStatusCode.OK) },
                    onFailure = { RequestResult.formatResult(result, HttpStatusCode.BadRequest) }
                )
                call.respond(HttpStatusCode.fromValue(response.httpStatus), response)
            } catch (e: Exception) {
                handleException(e)
            }
        }

        put() {
            val userId = retrieveUserId(call)
            try {
                val response = if (call.request.isMultipart()) {
                    multipart(call) { dto, image ->
                        val result = service.updateAccount(userId, dto, image)
                        result.fold(
                            onSuccess = { RequestResult.formatResult(result, HttpStatusCode.OK) },
                            onFailure = { RequestResult.formatResult(result, HttpStatusCode.BadRequest) }
                        )
                    }
                } else {
                    val dto = call.receive<UserDTO>()
                    val result = service.updateAccount(userId, dto)
                    result.fold(
                        onSuccess = { RequestResult.formatResult(result, HttpStatusCode.OK) },
                        onFailure = { RequestResult.formatResult(result, HttpStatusCode.BadRequest) }
                    )
                }
                call.respond(HttpStatusCode.fromValue(response.httpStatus), response)
                notifyAllClients(SocketMessage.RECIPE_UPDATED)
            } catch (e: Exception) {
                handleException(e)
            }
        }

        delete() {
            val userId = retrieveUserId(call)
            try {
                val result = service.deleteAccount(userId)
                val response = result.fold(
                    onSuccess = { RequestResult.formatResult(result, HttpStatusCode.OK) },
                    onFailure = { RequestResult.formatResult(result, HttpStatusCode.BadRequest) }
                )
                call.respond(HttpStatusCode.fromValue(response.httpStatus), response)
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }
}