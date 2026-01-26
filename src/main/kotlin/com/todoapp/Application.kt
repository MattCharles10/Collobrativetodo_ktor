package com.todoapp

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureMonitoring()
    configureSockets()
    configureHTTP()
    configureSecurity()
    configureSerialization()
    configureDatabase()
    configureRouting()
}