package com.acetylsalicylsaeure.platten.data

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DatabaseInitializer(private val context: Context) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            initializeExercises()
        }
    }

    private suspend fun initializeExercises() {
        val database = AppDatabase.getDatabase(context)
        val exerciseDao = database.exerciseDao()
        val initialExercises = listOf(
            Exercise(1, "Bench Press", 2.5),
            Exercise(2, "Squat", 2.5),
            Exercise(3, "Deadlift", 2.5),
            Exercise(4, "Overhead Press", 1.25),
            Exercise(5, "Barbell Row", 2.5)
        )
        initialExercises.forEach { exercise ->
            exerciseDao.insertExercise(exercise)
        }
    }
}
