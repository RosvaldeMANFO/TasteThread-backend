package com.florientmanfo.com.florientmanfo.utils

import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime

enum class IDSuffix {
    RECIPE,
    INGREDIENT,
    USER,
    COMMENT,
    LIKE
}

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun LocalDateTime.toLong() = this.toInstant(java.time.ZoneOffset.UTC).toEpochMilli()