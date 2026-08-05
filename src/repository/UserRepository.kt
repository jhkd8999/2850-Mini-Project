package org.jetbrains.amper.ktor.repository

import org.jetbrains.amper.ktor.database.Users
import org.jetbrains.amper.ktor.models.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepository {

    fun addUser(user: User) {

        transaction {

            Users.insert {

                it[firstName] = user.firstName
                it[lastName] = user.lastName
                it[email] = user.email
                it[address] = user.address
                it[password] = user.password

            }

        }

    }
    
    fun findByEmail(emailAddress: String): User? {

        return transaction {

            Users
                .selectAll()
                .where { Users.email eq emailAddress }
                .singleOrNull()
                ?.let {

                    User(
                        id = it[Users.id],
                        firstName = it[Users.firstName],
                        lastName = it[Users.lastName],
                        email = it[Users.email],
                        address = it[Users.address],
                        password = it[Users.password]
                    )

                }

        }

    }
    

}