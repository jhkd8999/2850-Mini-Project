package org.jetbrains.amper.ktor.models

data class Loan(
    val id: Int,
    val userId: Int, 
    val bookId: Int,
    val bookTitle: String, 
    val checkedOutAt: Long,
    val dueAt: Long
)