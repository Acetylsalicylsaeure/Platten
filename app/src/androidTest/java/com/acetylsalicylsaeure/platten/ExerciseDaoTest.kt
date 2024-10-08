package com.acetylsalicylsaeure.platten

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.acetylsalicylsaeure.platten.data.AppDatabase
import com.acetylsalicylsaeure.platten.data.Exercise
import com.acetylsalicylsaeure.platten.data.ExerciseDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class ExerciseDaoTest {
    private lateinit var exerciseDao: ExerciseDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        exerciseDao = db.exerciseDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetExercise() = runTest {
        val exercise = Exercise(0, "Bench Press", 2.5)
        val id = exerciseDao.insertExercise(exercise)
        assertTrue(id > 0)

        val retrievedExercise = exerciseDao.getExerciseById(id.toInt()).first()
        assertNotNull(retrievedExercise)
        assertEquals("Bench Press", retrievedExercise?.name)
        assertEquals(2.5, retrievedExercise?.weightSteps!!, 0.01)
    }

    @Test
    fun insertDuplicateExercise() = runTest {
        val exercise1 = Exercise(name = "Squat", weightSteps = 2.5)
        val exercise2 = Exercise(name = "Deadlift", weightSteps = 5.0)

        val id1 = exerciseDao.insertExercise(exercise1)
        assertTrue(id1 > 0)

        val id2 = exerciseDao.insertExercise(exercise2)
        assertTrue(id2 > 0)
        assertNotEquals(id1, id2)

        val retrievedExercise1 = exerciseDao.getExerciseById(id1.toInt()).first()
        assertNotNull(retrievedExercise1)
        assertEquals("Squat", retrievedExercise1?.name)
        assertEquals(2.5, retrievedExercise1?.weightSteps!!, 0.01)

        val retrievedExercise2 = exerciseDao.getExerciseById(id2.toInt()).first()
        assertNotNull(retrievedExercise2)
        assertEquals("Deadlift", retrievedExercise2?.name)
        assertEquals(5.0, retrievedExercise2?.weightSteps!!, 0.01)
    }

    @Test
    fun getAllExercises() = runTest {
        val exercise1 = Exercise(0, "Bench Press", 2.5)
        val exercise2 = Exercise(0, "Squat", 2.5)
        val exercise3 = Exercise(0, "Deadlift", 5.0)

        exerciseDao.insertExercise(exercise1)
        exerciseDao.insertExercise(exercise2)
        exerciseDao.insertExercise(exercise3)

        val allExercises = exerciseDao.getAllExercises().first()
        assertEquals(3, allExercises.size)
    }

    @Test
    fun updateExercise() = runTest {
        val exercise = Exercise(0, "Bench Press", 2.5)
        val id = exerciseDao.insertExercise(exercise)

        val updatedExercise = Exercise(id.toInt(), "Incline Bench Press", 1.25)
        exerciseDao.updateExercise(updatedExercise)

        val retrievedExercise = exerciseDao.getExerciseById(id.toInt()).first()
        assertNotNull(retrievedExercise)
        assertEquals("Incline Bench Press", retrievedExercise?.name)
        assertEquals(1.25, retrievedExercise?.weightSteps!!, 0.01)
    }

    @Test
    fun deleteExercise() = runTest {
        val exercise = Exercise(0, "Bench Press", 2.5)
        val id = exerciseDao.insertExercise(exercise)

        val retrievedExercise = exerciseDao.getExerciseById(id.toInt()).first()
        assertNotNull(retrievedExercise)

        exerciseDao.deleteExercise(retrievedExercise!!)

        val deletedExercise = exerciseDao.getExerciseById(id.toInt()).first()
        assertNull(deletedExercise)
    }
}
