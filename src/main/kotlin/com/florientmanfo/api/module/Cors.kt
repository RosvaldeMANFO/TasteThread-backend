package com.florientmanfo.com.florientmanfo.api.module

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.plugins.cors.routing.CORS
import org.koin.ktor.ext.inject
import kotlin.getValue

fun Application.configureCors() {
    val config: ApplicationConfig by inject()
    install(CORS) {
        val host = config.property("ktor.cors.host").getString()
        allowHost(host)

        HttpMethod.DefaultMethods.forEach(::allowMethod)

        allowHeaders { true }
        allowHeader(HttpHeaders.UserAgent)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowCredentials = true
        allowNonSimpleContentTypes = true
    }
}