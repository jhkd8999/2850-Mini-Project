package org.jetbrains.amper.ktor.database

import org.jetbrains.exposed.sql.Table

object Loans : Table() {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id)
    val bookId = integer("book_id").references(Books.id)
    val checkedOutAt = long("checked_out_at")
    val dueAt = long("due_at")
    val returnedAt = long("returned_at").nullable() // null=not returned yet
    override val primaryKey = PrimaryKey(id)
}