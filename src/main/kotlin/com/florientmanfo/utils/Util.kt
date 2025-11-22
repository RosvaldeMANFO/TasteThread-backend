package com.florientmanfo.utils

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime

enum class IDSuffix {
    RECIPE,
    INGREDIENT,
    USER,
    COMMENT,
}

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun LocalDateTime.toLong() = this.toInstant(java.time.ZoneOffset.UTC).toEpochMilli()

suspend fun RoutingContext.handleException(e: Exception) {
    val result = Result.failure<String>(e)
    val response = RequestResult.formatResult(result, HttpStatusCode.InternalServerError)
    call.respond(HttpStatusCode.fromValue(response.httpStatus), response)
}