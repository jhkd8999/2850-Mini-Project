package org.jetbrains.amper.ktor

import io.ktor.server.application.*
import org.jetbrains.amper.ktor.database.DatabaseTemplate

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    
    DatabaseTemplate.init()
    
    configureTemplates()
    configureSessions()
    configureAuthentication()
    configureRouting()
}
