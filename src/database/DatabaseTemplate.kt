package org.jetbrains.amper.ktor.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseTemplate {

    fun init() {

        Database.connect(
            url = "jdbc:sqlite:library.db",
            driver = "org.sqlite.JDBC"
        )

        transaction {
            SchemaUtils.create(Users,Books,Loans)
        }
        BookCsvImporter.importIfEmpty()
    }
}