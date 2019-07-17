package brotifypacha.scheduler.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users_table")
data class User (
    @PrimaryKey
    val _id: String,
    val username: String
)