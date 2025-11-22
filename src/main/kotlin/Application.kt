package com.florientmanfo

import com.florientmanfo.api.module.configureWebSockets
import com.florientmanfo.api.module.*
import com.florientmanfo.api.routing.configureRouting
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureDI()
    configureCors()
    configureWebSockets()
    configureLogging()
    configureAuthentication()
    configureRouting()
    configureSerialization()
    configureDatabase()
}