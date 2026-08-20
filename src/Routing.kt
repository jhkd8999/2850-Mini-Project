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
import org.jetbrains.amper.ktor.service.RecommendationService

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
            
            post("/books/{id}/checkout"){
                val session = call.principal<UserSession>()
                val user = session?.let{UserRepository().findByEmail(it.email)}
                val bookId = call.parameters["id"]?.toIntOrNull()
                
                if (user?.id == null || bookId == null) {
                    call.respondRedirect("/catalogue") 
                    return@post
                }
                
                val success = LoanRepository().checkoutBook(user.id, bookId)
                if (success) {
                    call.respondRedirect("/account")
                } else { call.respondRedirect("/catalogue?error=Book unavailable") }
            }
            
            post("/books/{id}/return"){
                val session = call.principal<UserSession>()
                val user = session?.let{UserRepository().findByEmail(it.email)}
                val bookId = call.parameters["id"]?.toIntOrNull()
                              
                if (user?.id == null || bookId == null) {
                    call.respondRedirect("/account") 
                    return@post
                }
                
                LoanRepository().returnBook(user.id,bookId)
                call.respondRedirect("/account")
            }
            
            get("/account") {
                val session = call.principal<UserSession>()
                val user = session?.let{UserRepository().findByEmail(it.email)}
                              
                if (user?.id == null) {
                    call.respondRedirect("/login") 
                    return@get
                }
                
                val loans = LoanRepository().getActiveLoansForUser(user.id)
                call.respondTemplate("account.peb", call.baseModel() + mapOf("user" to user, "loans" to loans))
            }
            
            get("/recommendations") {
                val session = call.principal<UserSession>()
                val user = session?.let{UserRepository().findByEmail(it.email)}
                
                if (user?.id == null) {
                    call.respondRedirect("/login")
                    return@get
                }
                
                val bookRepository = BookRepository()
                val loanRepository = LoanRepository()
                val recommendationService = RecommendationService()
                
                val recommendations = recommendationService.recommend(
                    allBooks = bookRepository.getAllBooks(),
                    borrowedBookIds = loanRepository.getBorrowedBookIdsForUser(user.id),
                    borrowCounts = loanRepository.getBorrowCounts(),
                    similarHistoryScores = loanRepository.getSimilarHistoryScores(user.id),
                    limit = 5
                )
                
                call.respondTemplate("recommendations.peb", call.baseModel() + mapOf("recommendations" to recommendations))
            }
            
        }
    }
}

private suspend fun ApplicationCall.homePage() {
    respondTemplate("index.peb", model = baseModel())
}

private suspend fun ApplicationCall.registrationPage() {
    respondTemplate("register.peb", model = baseModel())
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
            baseModel() + mapOf("error" to "Email already linked to an account, please log in or try again.")
        )
        return
    }

    if (password.length < 8) {
        respondTemplate(
            "register.peb",
            baseModel() + mapOf("error" to "Password must be at least 8 characters.")
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
        baseModel() + mapOf("message" to "Account created")
    )
}

private suspend fun ApplicationCall.loginPage() {
    respondTemplate("login.peb", model = baseModel())
}


private suspend fun ApplicationCall.cataloguePage() {
    respondTemplate(
        "catalogue.peb",
        model = baseModel() + mapOf(
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
        model = baseModel() + mapOf(
            "books" to books,
            "query" to query,
            "searched" to true
        )
    )
}


private suspend fun ApplicationCall.baseModel(): Map<String, Any> {
    val session = sessions.get<UserSession>()
    val user = session?.let{UserRepository().findByEmail(it.email)}
    return mapOf("loggedIn" to (user != null),"firstName" to (user?.firstName ?: ""))
}