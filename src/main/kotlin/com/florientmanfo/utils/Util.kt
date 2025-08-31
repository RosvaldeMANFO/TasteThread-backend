package com.florientmanfo.com.florientmanfo.utils

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