package org.jetbrains.amper.ktor

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.pebble.respondTemplate
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

import org.jetbrains.amper.ktor.models.Book
import org.jetbrains.amper.ktor.models.User
import org.jetbrains.amper.ktor.repository.BookRepository
import org.jetbrains.amper.ktor.repository.LoanRepository
import org.jetbrains.amper.ktor.repository.UserRepository

fun Application.configureRouting() {
    routing {
        // Map URLs onto request handling code here
        get("/") { call.homePage() }
        get("/login") { call.loginPage() }
        get("/register") { call.registrationPage() }
        post("/register") { call.registerUser() }
        
        authenticate("auth-form"){
            post("/login") {
                val principal = call.principal<UserIdPrincipal>()
                if (principal != null) {
                    call.sessions.set(
                        UserSession(principal.name)
                    )
                    call.respondRedirect("/catalogue")
                }
            }
        }
        authenticate("auth-session") {
            get("/catalogue") { call.cataloguePage() }
            get("/search") {call.searchCatalogue() }
            get("/logout") {
                call.sessions.clear<UserSession>()
                call.respondRedirect("/login")
            }
        }
    }
}

private suspend fun ApplicationCall.homePage() {
    respondTemplate("index.peb", model = emptyMap())
}

private suspend fun ApplicationCall.registrationPage() {
    respondTemplate("register.peb", model = emptyMap())
}

private suspend fun ApplicationCall.registerUser() {
    val repository = UserRepository()
    val params = receiveParameters()

    val firstName = params["firstName"] ?: ""
    val lastName = params["lastName"] ?: ""
    val email = params["email"] ?: ""
    val address = params["address"] ?: ""
    val password = params["password"] ?: ""

    if (repository.findByEmail(email) != null) {
        respondTemplate(
            "register.peb",
            mapOf(
                "error" to "Email already linked to an account, please log in or try again."
            )
        )
        return
    }

    if (password.length < 8) {
        respondTemplate(
            "register.peb",
            mapOf(
                "error" to "Password must be at least 8 characters."
            )
        )
        return
    }

    repository.addUser(
        User(
            firstName = firstName,
            lastName = lastName,
            email = email,
            address = address,
            password = password
        )
    )

    respondTemplate(
        "login.peb",
        mapOf(
            "message" to "Account created"
        )
    )
}

private suspend fun ApplicationCall.loginPage() {
    respondTemplate("login.peb", model = emptyMap())
}


private suspend fun ApplicationCall.cataloguePage() {
    respondTemplate(
        "catalogue.peb",
        model = mapOf(
            "books" to emptyList<Book>(),
            "query" to "",
            "searched" to false
        )
    )
}

private suspend fun ApplicationCall.searchCatalogue() {

    val query = request.queryParameters["book"] ?: ""

    val repository = BookRepository()

    val books = repository.searchBooks(query)

    respondTemplate(
        "catalogue.peb",
        model = mapOf(
            "books" to books,
            "query" to query,
            "searched" to true
        )
    )
}

