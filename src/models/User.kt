package org.jetbrains.amper.ktor.models

data class User(
    val id: Int? = null,
    val firstName: String,
    val lastName: String,
    val email: String,
    val address: String,
    val password: String
)