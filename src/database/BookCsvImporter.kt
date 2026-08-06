package org.jetbrains.amper.ktor.database

import org.apache.commons.csv.CSVFormat
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object BookCsvImporter {

    fun importIfEmpty() {

        transaction {

            // Don't import everything again every time the server starts
            if (Books.selectAll().count() > 0) {
                return@transaction
            }

            val stream =
                BookCsvImporter::class.java
                    .classLoader
                    .getResourceAsStream("library_booklist.csv")
                    ?: error("Could not find library_booklist.csv")

            stream.bufferedReader().use { reader ->

                val records = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .get()
                    .parse(reader)

                for (record in records) {

                    Books.insert {

                        it[title] =
                            record["title"].trim()

                        it[author] =
                            record["author"].trim()

                        it[isbn] =
                            record["isbn_13"]
                                .trim()
                                .ifBlank { null }

                        it[format] =
                            record["format_code"].trim()

                        it[location] =
                            record["location_code"]
                                .trim()
                                .ifBlank { null }

                        it[notes] =
                            record["notes"]
                                .trim()
                                .ifBlank { null }

                        it[available] = true
                    }
                }
            }
        }
    }
}