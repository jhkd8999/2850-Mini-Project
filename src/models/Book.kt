package org.jetbrains.amper.ktor.models

data class Book(
    val id: Int? = null,
    val title: String,
    val author: String,
    val isbn: String?,
    val format: String,
    val location: String?,
    val notes: String?,
    val available: Boolean
)