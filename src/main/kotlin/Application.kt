package com.florientmanfo

import com.florientmanfo.api.module.configureWebSockets
import com.florientmanfo.com.florientmanfo.api.module.*
import com.florientmanfo.com.florientmanfo.api.routing.configureRouting
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureCORS()
    configureWebSockets()
    configureLogging()
    configureAuthentication()
    configureDI()
    configureRouting()
    configureSerialization()
    configureDatabase()
}