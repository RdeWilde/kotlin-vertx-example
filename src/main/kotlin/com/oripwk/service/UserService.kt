package com.oripwk.service

import com.oripwk.dal.Users
import com.oripwk.execBlocking
import com.oripwk.model.User
import com.oripwk.retry
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.experimental.launch
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SchemaUtils.createMissingTablesAndColumns
import org.jetbrains.exposed.sql.transactions.transaction

class UserService(private val vx: Vertx, config: JsonObject) {
    init {
        Database.connect(
                url = config.getString("url"),
                user = config.getString("user"),
                password = config.getString("password"),
                driver = config.getString("driver")
        )

        launch {
            retry(times = 3, interval = 3000) {
                transaction {
                    createMissingTablesAndColumns(Users)
                }
            }
        }
    }

    suspend fun create(user: User): Unit = execBlocking(vx) {
        transaction {
            Users.insert { Users.of(user, it) }
        }
    }

    suspend fun updateById(user: User, id: Int): Unit = execBlocking(vx) {
        transaction {
            Users.update({ Users.id eq id }) {
                Users.of(user, it)
            }
        }
    }

    suspend fun getById(id: Int): User = execBlocking(vx) {
        transaction {
            Users.map(Users.select { Users.id eq id }.first())
        }
    }

    suspend fun deleteById(id: Int): Unit = execBlocking(vx) {
        transaction {
            Users.deleteWhere { Users.id eq id }
        }
    }

    suspend fun getAll(): List<User> = execBlocking(vx) {
        transaction {
            Users.selectAll().map(Users::map)
        }
    }
}