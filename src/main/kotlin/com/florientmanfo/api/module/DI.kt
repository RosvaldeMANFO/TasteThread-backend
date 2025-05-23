package com.florientmanfo.com.florientmanfo.api.module

import com.florientmanfo.com.florientmanfo.data.repository.repositoryModule
import com.florientmanfo.com.florientmanfo.services.serviceModule
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun Application.configureDI() {
    install(Koin) {
        modules(
            module {
                single { environment.config }
            },
            repositoryModule,
            serviceModule
        )
    }
}