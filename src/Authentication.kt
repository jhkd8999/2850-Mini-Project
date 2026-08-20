package org.jetbrains.amper.ktor

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.form
import io.ktor.server.auth.session
import io.ktor.server.pebble.respondTemplate
import io.ktor.server.response.respondRedirect
import org.jetbrains.amper.ktor.repository.UserRepository

fun Application.configureAuthentication() {
    val repository = UserRepository()
    install(Authentication) {
        
        form("auth-form") {
            userParamName = "email"
            passwordParamName = "password"
            
            validate {credentials ->
                val user = repository.findByEmail(credentials.name)
                
                if (user != null && user.password == credentials.password) {
                    UserIdPrincipal(credentials.name)
                } else {null}
                }
            
            challenge {
                call.respondTemplate("login.peb", mapOf("error" to "Invalid email or password"))
            }
        }
        session<UserSession>("auth-session") {
            validate { session ->
                val user = repository.findByEmail(session.email)
                if (user != null){
                    session
                } else {null}
            }
            challenge{call.respondRedirect("/login")}
        }
        
    }
}