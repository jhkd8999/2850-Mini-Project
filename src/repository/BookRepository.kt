package org.jetbrains.amper.ktor.repository

import org.jetbrains.amper.ktor.database.Books
import org.jetbrains.amper.ktor.models.Book
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class BookRepository {

    fun addBook(book: Book) {
        transaction {
            Books.insert {
                it[title] = book.title
                it[author] = book.author
                it[isbn] = book.isbn
                it[format] = book.format
                it[location] = book.location
                it[notes] = book.notes
                it[available] = book.available
            }
        }
    }

    fun getAllBooks(): List<Book> {
        return transaction {

            Books.selectAll().map {

                Book(
                    id = it[Books.id],
                    title = it[Books.title],
                    author = it[Books.author],
                    isbn = it[Books.isbn],
                    format = it[Books.format],
                    location = it[Books.location],
                    notes = it[Books.notes],
                    available = it[Books.available]
                )
            }
        }
    }
    
    fun searchBooks(query: String): List<Book> {
        return transaction {
            Books
                .selectAll()
                .where { Books.title like "%$query%" }
                .map {
                    Book(
                        id = it[Books.id],
                        title = it[Books.title],
                        author = it[Books.author],
                        isbn = it[Books.isbn],
                        format = it[Books.format],
                        location = it[Books.location],
                        notes = it[Books.notes],
                        available = it[Books.available]
                    )
                }
        }
    }

}