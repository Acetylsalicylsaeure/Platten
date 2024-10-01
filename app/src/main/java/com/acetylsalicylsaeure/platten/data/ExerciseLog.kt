package com.acetylsalicylsaeure.platten.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "exercise_logs",
    foreignKeys = [ForeignKey(
        entity = Exercise::class,
        parentColumns = ["id"],
        childColumns = ["exerciseId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class ExerciseLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val exerciseId: Int,
    val date: Date,
    val weight: Float,
    val reps: Int
)
