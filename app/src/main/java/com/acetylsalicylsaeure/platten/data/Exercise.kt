package com.acetylsalicylsaeure.platten.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    @ColumnInfo(name = "weight_steps") val weightSteps: Double,
    @ColumnInfo(name = "hidden") val hidden: Boolean = false
)