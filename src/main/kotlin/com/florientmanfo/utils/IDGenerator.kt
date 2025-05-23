package com.florientmanfo.com.florientmanfo.utils

import java.util.*

object IDGenerator {
    private const val UID_PREFIX = "COOK"

    fun generate(suffix: IDSuffix): String {
        val uuid = UUID.randomUUID().toString()
        return "$UID_PREFIX-${getCurrentDate()}-${uuid.take(10)}-${suffix.name}"
    }

    private fun getCurrentDate(): String {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")
        return java.time.LocalDate.now().format(formatter)
    }
}