package com.acetylsalicylsaeure.platten

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.acetylsalicylsaeure.platten.data.AppDatabase
import com.acetylsalicylsaeure.platten.data.DatabaseBackupManager
import com.acetylsalicylsaeure.platten.data.Exercise
import com.acetylsalicylsaeure.platten.data.ExerciseLog
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.Date
import kotlinx.coroutines.runBlocking

@RunWith(AndroidJUnit4::class)
class DatabaseBackupManagerTest {

    private lateinit var backupManager: DatabaseBackupManager
    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = AppDatabase.getDatabase(context)
        backupManager = DatabaseBackupManager(context)
    }

    @Test
    fun testBackupAndRestore() {
        runBlocking {
            // Prepare test data
            val exercise1 = Exercise(1, "Squat", 2.5)
            val exercise2 = Exercise(2, "Bench Press", 1.25)
            val log1 = ExerciseLog(1, 1, Date(), 100f, 5)
            val log2 = ExerciseLog(2, 2, Date(), 80f, 8)

            database.exerciseDao().insertExercise(exercise1)
            database.exerciseDao().insertExercise(exercise2)
            database.exerciseLogDao().insertLog(log1)
            database.exerciseLogDao().insertLog(log2)

            // Perform backup
            val backupDir = File(InstrumentationRegistry.getInstrumentation().targetContext.filesDir, "backup_test")
            backupDir.mkdirs()
            backupManager.backupDatabase(backupDir)

            // Verify backup files exist
            val exercisesFile = File(backupDir, "exercises.csv")
            val logsFile = File(backupDir, "logs.csv")
            assertTrue("Exercises backup file should exist", exercisesFile.exists())
            assertTrue("Logs backup file should exist", logsFile.exists())

            // Clear the database
            database.exerciseDao().deleteAllExercises()
            database.exerciseLogDao().deleteAllLogs()

            // Restore from backup
            backupManager.restoreDatabase(backupDir)

            // Verify restored data
            val restoredExercises = database.exerciseDao().getAllExercisesSync()
            val restoredLogs = database.exerciseLogDao().getAllLogsSync()

            assertEquals("Should have restored 2 exercises", 2, restoredExercises.size)
            assertEquals("First exercise should be Squat", "Squat", restoredExercises[0].name)
            assertEquals("Squat weight steps should be 2.5", 2.5, restoredExercises[0].weightSteps, 0.01)
            assertEquals("Second exercise should be Bench Press", "Bench Press", restoredExercises[1].name)
            assertEquals("Bench Press weight steps should be 1.25", 1.25, restoredExercises[1].weightSteps, 0.01)

            assertEquals("Should have restored 2 logs", 2, restoredLogs.size)
            assertEquals("First log should be for exercise 1", 1, restoredLogs[0].exerciseId)
            assertEquals("First log weight should be 100", 100f, restoredLogs[0].weight, 0.01f)
            assertEquals("First log reps should be 5", 5, restoredLogs[0].reps)
            assertEquals("Second log should be for exercise 2", 2, restoredLogs[1].exerciseId)
            assertEquals("Second log weight should be 80", 80f, restoredLogs[1].weight, 0.01f)
            assertEquals("Second log reps should be 8", 8, restoredLogs[1].reps)
        }
    }
}