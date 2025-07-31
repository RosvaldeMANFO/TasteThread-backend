package com.florientmanfo.com.florientmanfo.api.module

import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.application.*
import org.slf4j.event.Level

fun Application.configureLogging() {
    install(CallLogging) {
        level = Level.INFO
    }
}