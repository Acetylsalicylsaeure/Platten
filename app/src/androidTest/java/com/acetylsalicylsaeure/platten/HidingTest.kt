package com.acetylsalicylsaeure.platten

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.acetylsalicylsaeure.platten.data.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*

@RunWith(AndroidJUnit4::class)
class HidingTest {
    private lateinit var exerciseDao: ExerciseDao
    private lateinit var exerciseLogDao: ExerciseLogDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).allowMainThreadQueries().build()
        exerciseDao = db.exerciseDao()
        exerciseLogDao = db.exerciseLogDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun testExerciseHiding() = runBlocking {
        // Create test exercise
        val exercise = Exercise(id = 1, name = "Test Exercise", weightSteps = 2.5, hidden = false)
        exerciseDao.insertExercise(exercise)

        // Verify initial state
        var visibleExercises = exerciseDao.getVisibleExercises().first()
        assertEquals(1, visibleExercises.size)
        assertFalse(visibleExercises[0].hidden)

        // Hide exercise
        exerciseDao.updateExerciseHidden(1, true)

        // Verify exercise is hidden
        visibleExercises = exerciseDao.getVisibleExercises().first()
        assertEquals(0, visibleExercises.size)

        // Verify exercise still exists in all exercises
        val allExercises = exerciseDao.getAllExercisesSync()
        assertEquals(1, allExercises.size)
        val hiddenExercise = allExercises.find { it.id == 1 }
        assertNotNull(hiddenExercise)
        assertTrue(hiddenExercise!!.hidden)
    }

    @Test
    fun testExerciseUnhiding() = runBlocking {
        // Create hidden exercise
        val exercise = Exercise(id = 1, name = "Test Exercise", weightSteps = 2.5, hidden = true)
        exerciseDao.insertExercise(exercise)

        // Verify initial state
        var visibleExercises = exerciseDao.getVisibleExercises().first()
        assertEquals(0, visibleExercises.size)

        // Unhide exercise
        exerciseDao.updateExerciseHidden(1, false)

        // Verify exercise is visible
        visibleExercises = exerciseDao.getVisibleExercises().first()
        assertEquals(1, visibleExercises.size)
        assertFalse(visibleExercises[0].hidden)
    }

    @Test
    fun testHiddenExerciseLogsRetention() = runBlocking {
        // Create exercise with logs
        val exercise = Exercise(id = 1, name = "Test Exercise", weightSteps = 2.5, hidden = false)
        exerciseDao.insertExercise(exercise)

        val now = Date()
        val log1 = ExerciseLog(id = 1, exerciseId = 1, date = now, weight = 100f, reps = 5)
        val log2 = ExerciseLog(id = 2, exerciseId = 1, date = now, weight = 102.5f, reps = 5)
        exerciseLogDao.insertLog(log1)
        exerciseLogDao.insertLog(log2)

        // Verify initial state
        var logs = exerciseLogDao.getLogsForExercise(1).first()
        assertEquals(2, logs.size)

        // Hide exercise
        exerciseDao.updateExerciseHidden(1, true)

        // Verify logs are retained
        logs = exerciseLogDao.getLogsForExercise(1).first()
        assertEquals(2, logs.size)

        // Verify exercise is hidden but logs are accessible
        val visibleExercises = exerciseDao.getVisibleExercises().first()
        assertEquals(0, visibleExercises.size)
        assertEquals(2, logs.size)
    }

    @Test
    fun testMultipleExercisesVisibility() = runBlocking {
        // Create multiple exercises with different visibility
        val exercise1 = Exercise(id = 1, name = "Exercise 1", weightSteps = 2.5, hidden = false)
        val exercise2 = Exercise(id = 2, name = "Exercise 2", weightSteps = 2.5, hidden = true)
        val exercise3 = Exercise(id = 3, name = "Exercise 3", weightSteps = 2.5, hidden = false)

        exerciseDao.insertExercise(exercise1)
        exerciseDao.insertExercise(exercise2)
        exerciseDao.insertExercise(exercise3)

        // Verify visible exercises
        val visibleExercises = exerciseDao.getVisibleExercises().first()
        assertEquals(2, visibleExercises.size)
        assertTrue(visibleExercises.any { it.name == "Exercise 1" })
        assertTrue(visibleExercises.any { it.name == "Exercise 3" })
        assertFalse(visibleExercises.any { it.name == "Exercise 2" })

        // Verify all exercises
        val allExercises = exerciseDao.getAllExercisesSync()
        assertEquals(3, allExercises.size)
    }
}