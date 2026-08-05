package org.jetbrains.amper.ktor.database

import org.jetbrains.exposed.sql.Table

object Users : Table() {

    val id = integer("id").autoIncrement()

    val firstName = varchar("first_name", 50)

    val lastName = varchar("last_name", 50)

    val email = varchar("email", 100).uniqueIndex()

    val address = varchar("address", 200)

    val password = varchar("password", 255)

    override val primaryKey = PrimaryKey(id)

}