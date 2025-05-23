package com.florientmanfo.com.florientmanfo.utils

import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class RequestResult<T>(
    val status: String,
    val data: T? = null,
    val error: String? = null,
    val httpStatus: Int
) {
    companion object {
        fun <T>formatResult(result: Result<T>, httpStatus: HttpStatusCode): RequestResult<T> =
           result.fold(
               onSuccess = { RequestResult("success", it, null, httpStatus.value) },
               onFailure = { RequestResult("error", null, it.message, httpStatus.value) }
           )
    }
}