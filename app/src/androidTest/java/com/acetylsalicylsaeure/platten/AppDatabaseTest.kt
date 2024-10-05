package com.acetylsalicylsaeure.platten.data

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var exerciseDao: ExerciseDao
    private lateinit var exerciseLogDao: ExerciseLogDao

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        exerciseDao = db.exerciseDao()
        exerciseLogDao = db.exerciseLogDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun testDatabaseCreation() {
        assertNotNull("Database should not be null", db)
        assertNotNull("ExerciseDao should not be null", exerciseDao)
        assertNotNull("ExerciseLogDao should not be null", exerciseLogDao)
    }

    @Test
    @Throws(Exception::class)
    fun testExerciseOperations() = runBlocking {
        // Insert an exercise
        val exercise = Exercise(id = 1, name = "Bench Press", weightSteps = 2.5)
        exerciseDao.insertExercise(exercise)

        // Retrieve and verify the exercise
        val retrievedExercises = exerciseDao.getAllExercises().first()
        assertEquals("Should have one exercise", 1, retrievedExercises.size)
        assertEquals("Exercise name should match", "Bench Press", retrievedExercises[0].name)
        assertEquals("Weight steps should match", 2.5, retrievedExercises[0].weightSteps, 0.01)

        // Update the exercise
        val updatedExercise = exercise.copy(name = "Updated Bench Press")
        exerciseDao.updateExercise(updatedExercise)

        // Retrieve and verify the updated exercise
        val updatedExercises = exerciseDao.getAllExercises().first()
        assertEquals("Exercise name should be updated", "Updated Bench Press", updatedExercises[0].name)

        // Delete the exercise
        exerciseDao.deleteExercise(updatedExercise)

        // Verify deletion
        val remainingExercises = exerciseDao.getAllExercises().first()
        assertTrue("Exercise list should be empty", remainingExercises.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun testExerciseLogOperations() = runBlocking {
        // First, insert an exercise
        val exercise = Exercise(id = 1, name = "Squat", weightSteps = 5.0)
        exerciseDao.insertExercise(exercise)

        // Insert an exercise log
        val log = ExerciseLog(id = 1, exerciseId = exercise.id, date = Date(), weight = 100f, reps = 5)
        exerciseLogDao.insertLog(log)

        // Retrieve and verify the log
        val retrievedLogs = exerciseLogDao.getLogsForExercise(exercise.id).first()
        assertEquals("Should have one log", 1, retrievedLogs.size)
        assertEquals("Log weight should match", 100f, retrievedLogs[0].weight)
        assertEquals("Log reps should match", 5, retrievedLogs[0].reps)

        // Delete the log
        exerciseLogDao.deleteLog(retrievedLogs[0])

        // Verify deletion
        val remainingLogs = exerciseLogDao.getLogsForExercise(exercise.id).first()
        assertTrue("Log list should be empty", remainingLogs.isEmpty())
    }

    /*
    @Test
    @Throws(Exception::class)
    fun testMigration1To2() {
        // Create version 1 of the database
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL("INSERT INTO exercises (id, name) VALUES (1, 'Old Exercise')")
            close()
        }

        // Migrate to version 2
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, AppDatabase.MIGRATION_1_2)

        // Verify that the new column exists and has the default value
        val cursor = db.query("SELECT weight_steps FROM exercises WHERE id = 1")
        cursor.moveToFirst()
        val weightSteps = cursor.getFloat(0)
        assertEquals("Default weight_steps should be 0.0", 0.0f, weightSteps)

        // Clean up
        db.close()
    }*/

    @Test
    @Throws(Exception::class)
    fun testGetDatabaseSingleton() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Get the database instance twice
        val db1 = AppDatabase.getDatabase(context)
        val db2 = AppDatabase.getDatabase(context)

        // Verify that both instances are the same (singleton pattern)
        assertSame("Database instances should be the same", db1, db2)

        // Clean up
        db1.close()
    }

    companion object {
        private const val TEST_DB = "migration-test"
    }
}
