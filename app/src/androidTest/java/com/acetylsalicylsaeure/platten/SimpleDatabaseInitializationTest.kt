package com.acetylsalicylsaeure.platten

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.acetylsalicylsaeure.platten.data.AppDatabase
import com.acetylsalicylsaeure.platten.test.TestDatabase
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SimpleDatabaseInitializationTest {

    @Test
    fun testDatabaseCreation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        println("Context type: ${context.javaClass.name}")

        try {
            val database = Room.inMemoryDatabaseBuilder(
                context,
                TestDatabase::class.java
            ).build()

            println("Test database created successfully")
            assertNotNull("Database should be initialized", database)
            assertNotNull("TestDao should be accessible", database.testDao())

            database.close()
        } catch (e: Exception) {
            println("Error creating test database: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }


    @Test
    fun testDatabaseInitialization() {
        // Get the context of the app under test
        val context = ApplicationProvider.getApplicationContext<Context>()

        println("Context type: ${context.javaClass.name}")

        try {
            // Attempt to create the database directly
            val database = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "platten_database"
            ).build()

            println("Database created successfully")

            // Assert that the database instance is not null
            assertNotNull("Database should be initialized", database)

            // Optionally, you can also check if the DAOs are accessible
            assertNotNull("ExerciseDao should be accessible", database.exerciseDao())
            assertNotNull("ExerciseLogDao should be accessible", database.exerciseLogDao())

            // Close the database
            database.close()
        } catch (e: Exception) {
            println("Error creating database: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}