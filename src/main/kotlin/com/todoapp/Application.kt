package com.todoapp

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    // 1. First configure monitoring and HTTP
    configureMonitoring()
    configureHTTP()

    // 2. Configure serialization (needed before security)
    configureSerialization()

    // 3. Configure security (MUST be before any authenticate() calls)
    configureSecurity()

    // 4. Configure database
    configureDatabase()

    // 5. Configure sockets (uses authenticate() - needs security first)
    configureSockets()

    // 6. Finally configure routing (also uses authenticate())
    configureRouting()
}