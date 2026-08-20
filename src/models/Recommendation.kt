package org.jetbrains.amper.ktor.models

data class Recommendation(
    val book: Book,
    val score: Int,
    val justifications: List<String>
)