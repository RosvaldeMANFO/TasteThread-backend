package com.florientmanfo

import com.florientmanfo.com.florientmanfo.api.module.*
import com.florientmanfo.com.florientmanfo.api.routing.configureRouting
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureLogging()
    configureAuthentication()
    configureDI()
    configureRouting()
    configureSerialization()
    configureDatabase()
}