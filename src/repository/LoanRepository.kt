package org.jetbrains.amper.ktor.repository

import org.jetbrains.amper.ktor.database.Books
import org.jetbrains.amper.ktor.database.Loans
import org.jetbrains.amper.ktor.models.Loan
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class LoanRepository {
    
    fun checkoutBook(userId: Int, bookId: Int): Boolean {
        
        return transaction {
            val book = Books.selectAll().where{Books.id eq bookId}.singleOrNull()
            
            if (book == null){return@transaction false}
            if (!book[Books.available]){return@transaction false}
            
            val now = System.currentTimeMillis()
            val twoWeeks = 14L * 24 * 60 *60 *1000
            Loans.insert {
                it[Loans.userId] = userId
                it[Loans.bookId] = bookId
                it[checkedOutAt] = now
                it[dueAt] = now+twoWeeks
                it[returnedAt] = null
            }
            Books.update({Books.id eq bookId}) {it[available] = false}
            
            true
        }
    }
    
    
    fun returnBook(userId: Int, bookId: Int): Boolean {
        return transaction{
            val loan = Loans.selectAll().where{(Loans.userId eq userId) and (Loans.bookId eq bookId) and Loans.returnedAt.isNull()}.singleOrNull()
            
            if (loan == null) {return@transaction false}
            
            Loans.update({Loans.id eq loan[Loans.id]}) {
                it[returnedAt] = System.currentTimeMillis()
            }
            
            Books.update({Books.id eq bookId}) {it[available] = true}
            
            true
        }
        
    }
    
    
    fun getActiveLoansForUser(userId: Int): List<Loan> {
        
        return transaction {
            (Loans innerJoin Books).selectAll().where{
                (Loans.userId eq userId) and
                Loans.returnedAt.isNull()
            }.map {row->
                Loan(id=row[Loans.id],
                    userId=row[Loans.userId],
                    bookId=row[Loans.bookId],
                    bookTitle=row[Books.title],
                    checkedOutAt=row[Loans.checkedOutAt],
                    dueAt=row[Loans.dueAt])}
        }
    }
    
    fun getBorrowedBookIdsForUser(userId: Int): List<Int> {
        return transaction {
            Loans.selectAll().where{Loans.userId eq userId}.map{it[Loans.bookId]}
        }
    }
    
    fun getBorrowCounts(): Map<Int, Int> {
        return transaction {
            Loans.selectAll().map{it[Loans.bookId]}.groupingBy{it}.eachCount()
        }
    }
    
    
}