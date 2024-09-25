package com.example.platten.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey val id: Int,
    val name: String,
    @ColumnInfo(name = "weight_steps") val weightSteps: Double // Kotlin property in camelCase, SQL column in snake_case
)
