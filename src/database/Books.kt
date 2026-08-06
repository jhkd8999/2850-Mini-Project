package org.jetbrains.amper.ktor.database

import org.jetbrains.exposed.sql.Table

object Books : Table() {

    val id = integer("id").autoIncrement()

    val title = varchar("title", 255)

    val author = varchar("author", 255)

    val isbn = varchar("isbn", 20).nullable()

    val format = varchar("format", 20)

    val location = varchar("location", 50).nullable()

    val notes = varchar("notes", 255).nullable()

    val available = bool("available").default(true)

    override val primaryKey = PrimaryKey(id)
}