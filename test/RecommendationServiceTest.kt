package org.jetbrains.amper.ktor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

import org.jetbrains.amper.ktor.models.Book
import org.jetbrains.amper.ktor.service.RecommendationService

class RecommendationServiceTest {

    private val service =
        RecommendationService()


    private val books = listOf(

        Book(
            id = 1,
            title = "A Caribbean Mystery",
            author = "Agatha Christie",
            isbn = null,
            format = "PB",
            location = "F1-B02-S03",
            notes = null,
            available = true
        ),

        Book(
            id = 2,
            title = "4:50 from Paddington",
            author = "Agatha Christie",
            isbn = null,
            format = "PB",
            location = "F1-B09-S05",
            notes = null,
            available = true
        ),

        Book(
            id = 3,
            title = "Dune",
            author = "Frank Herbert",
            isbn = null,
            format = "PB",
            location = "F2-B01-S01",
            notes = null,
            available = true
        )
    )


    @Test
    fun `books by preferred authors rank higher`() {

        val recommendations =
            service.recommend(
                allBooks = books,
                borrowedBookIds = listOf(1),
                borrowCounts = emptyMap()
            )

        assertEquals(
            "4:50 from Paddington",
            recommendations.first().book.title
        )
    }


    @Test
    fun `already borrowed books are excluded`() {

        val recommendations =
            service.recommend(
                allBooks = books,
                borrowedBookIds = listOf(1),
                borrowCounts = emptyMap()
            )

        assertFalse(
            recommendations.any {
                it.book.title == "A Caribbean Mystery"
            }
        )
    }


    @Test
    fun `unavailable books are excluded`() {

        val unavailableBook =
            books[1].copy(
                available = false
            )

        val testBooks = listOf(
            books[0],
            unavailableBook,
            books[2]
        )

        val recommendations =
            service.recommend(
                allBooks = testBooks,
                borrowedBookIds = listOf(1),
                borrowCounts = emptyMap()
            )

        assertFalse(
            recommendations.any {
                it.book.title == "4:50 from Paddington"
            }
        )
    }


    @Test
    fun `recommendation limit is respected`() {

        val recommendations =
            service.recommend(
                allBooks = books,
                borrowedBookIds = emptyList(),
                borrowCounts = emptyMap(),
                limit = 2
            )

        assertEquals(
            2,
            recommendations.size
        )
    }


    @Test
    fun `popular books are recommended to users with no history`() {

        val recommendations =
            service.recommend(
                allBooks = books,
                borrowedBookIds = emptyList(),
                borrowCounts = mapOf(
                    1 to 1,
                    2 to 2,
                    3 to 10
                )
            )

        assertEquals(
            "Dune",
            recommendations.first().book.title
        )
    }
}