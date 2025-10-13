package com.florientmanfo.api.module

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.collections.remove
import kotlin.time.Duration.Companion.seconds

val clients = CopyOnWriteArrayList<DefaultWebSocketServerSession>()

fun Application.configureWebSockets() {
    install(WebSockets) {
        pingPeriod = 60.seconds
        timeout = 120.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
}

fun Route.webSocket() {
    webSocket("/ws") {
        clients.add(this)
        try {
            for (frame in incoming) {
                if(frame is Frame.Ping){
                    send(Frame.Pong(frame.data))
                }
                if(frame is Frame.Close) {
                    break
                }
            }
        } finally {
            clients.remove(this)
        }
    }
}

suspend fun notifyAllClients(message: SocketMessage) {
    val failedClients = mutableListOf<DefaultWebSocketServerSession>()

    for (session in clients) {
        try {
            val frame = Frame.Text(message.name)
            session.send(frame)
        } catch (e: Exception) {
            failedClients.add(session)
        }
    }

    for (session in failedClients) {
        try {
            val frame = Frame.Text(message.name)
            session.send(frame)
        } catch (e: Exception) {
            clients.remove(session)
        }
    }
}

enum class SocketMessage {
    RECIPE_CREATED,
    RECIPE_UPDATED,
    RECIPE_DELETED,
    RECIPE_LIKED,
    RECIPE_COMMENTED;
}