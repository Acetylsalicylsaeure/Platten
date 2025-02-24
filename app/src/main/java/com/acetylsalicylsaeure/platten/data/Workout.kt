package com.acetylsalicylsaeure.platten.data

import androidx.room.*
import java.util.Date

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val lastViewed: Date
)

@Entity(
    tableName = "workout_exercise_cross_ref",
    primaryKeys = ["workoutId", "exerciseId"],
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("workoutId"),
        Index("exerciseId")
    ]
)
data class WorkoutExerciseCrossRef(
    val workoutId: Int,
    val exerciseId: Int,
    val orderPosition: Int
)

data class WorkoutWithExercises(
    @Embedded val workout: Workout,
    @Relation(
        entity = Exercise::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = WorkoutExerciseCrossRef::class,
            parentColumn = "workoutId",
            entityColumn = "exerciseId"
        )
    )
    val exercises: List<Exercise>
)