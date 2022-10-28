package com.sirekanyan.andersrobot.repository

import com.sirekanyan.andersrobot.repository.table.Cities
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

interface CityRepository {
    fun getCities(chat: Long): List<Long>
    fun putCity(chat: Long, city: Long): Boolean
    fun deleteCity(chat: Long, city: Long): Boolean
}

class CityRepositoryImpl(url: String) : CityRepository {

    init {
        Database.connect(url)
        transaction {
            SchemaUtils.create(Cities)
        }
    }

    override fun getCities(chat: Long): List<Long> = transaction {
        Cities.select { Cities.chat eq chat }.map { it[Cities.city] }
    }

    override fun putCity(chat: Long, city: Long): Boolean = transaction {
        Cities.insertIgnore {
            it[Cities.chat] = chat
            it[Cities.city] = city
        }
        true
    }

    override fun deleteCity(chat: Long, city: Long): Boolean = transaction {
        Cities.deleteWhere { (Cities.chat eq chat) and (Cities.city eq city) } != 0
    }

}