package org.jetbrains.amper.ktor.service

import org.jetbrains.amper.ktor.models.Book
import org.jetbrains.amper.ktor.models.Recommendation

class RecommendationService {
    
    fun recommend(allBooks: List<Book>, borrowedBookIds: List<Int>, borrowCounts: Map<Int, Int>, limit: Int = 5): List<Recommendation> {
        
        val borrowedBooks = allBooks.filter { it.id in borrowedBookIds}
        val preferredAuthors = borrowedBooks.groupingBy {it.author}.eachCount()
        val preferredFormats = borrowedBooks.groupingBy {it.format}.eachCount()
        val borrowedTitles = borrowedBooks.map {titleKey(it)}.toSet()
        
        val bookGroups = allBooks.filter {it.available}.filter{titleKey(it) !in borrowedTitles}.groupBy{titleKey(it)}
        
        val recommendations = bookGroups.map { ( , copies) ->
            val book = copies.first()
            val score = 0
            val justifications = mutableListOf<String>()
            
            val authorBorrowCount = preferredAuthors[book.author] ?: 0
            if (authorBorrowCount > 0) {
                score += authorBorrowCount * 5 // most significant boost in recomendation 
                justifications.add("You have borrowed books by ${book.author}")
            }
            
            val formatBorrowCount = preferredFormats[book.format] ?: 0
            if (formatBorrowCount > 0) {
                score += formatBorrowCount * 2 // mid boost in recomendation
                justifications.add("You have borrowed books in ${book.format} format")
            }
            
            val popularity = copies.sumOf { copy ->
                if (copy.id == null) {0} else {borrowCounts[copy.id] ?: 0}
            }
            
            score += popularity
            if (popularity > 0) {
                justifications.add("This book is popular with other library customers")
            }
            
            Recommendation(
                book = book,
                score = score,
                justifications = justifications
            )
        }
        
        return recommendations.sortedByDescending{it.score}.take(limit)
    }
    
    private fun titleKey(book: Book): String {
        return "${book.title.lowercase()}|${book.author.lowercase()}"
    }
}