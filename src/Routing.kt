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
        get("/") { call.homePage() }
        get("/login") { call.loginPage() }
        get("/register") { call.registrationPage() }
        post("/register") { call.registerUser() }
    }
}

private suspend fun ApplicationCall.homePage() {
    respondTemplate("index.peb", model = emptyMap())
}

private suspend fun ApplicationCall.registrationPage() {
    respondTemplate("register.peb", model = emptyMap())
}

private suspend fun ApplicationCall.registerUser() {

    val params = receiveParameters()

    val email = params["email"]
    val password = params["password"]


    if(password == null || password.length < 8){

        respondTemplate("register.peb",mapOf("error" to "Password must be at least 8 characters" ))
    } else {

        respondTemplate("login.peb",mapOf("message" to "Account created"))
    }
}

private suspend fun ApplicationCall.loginPage() {
    respondTemplate("login.peb", model = emptyMap())
}