package org.jetbrains.amper.ktor

import io.ktor.http.Parameters
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.receiveParameters
import io.ktor.server.pebble.respondTemplate

fun Application.configureRouting() {
    routing {
        // Map URLs onto request handling code here
        get("/") { call.displayForm() }
    }
}

private suspend fun ApplicationCall.displayForm() {
    respondTemplate("base.peb", model = emptyMap())
}