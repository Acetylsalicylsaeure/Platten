package com.acetylsalicylsaeure.platten.data

import android.content.Context
import androidx.room.Room
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseBackupRestore(private val context: Context) {

    private val dbName = "platten_database"
    private val backupFileName = "platten_database_backup.db"

    suspend fun exportDatabase(): Boolean = withContext(Dispatchers.IO) {
        try {
            val currentDB = context.getDatabasePath(dbName)
            val backupDB = File(context.getExternalFilesDir(null), backupFileName)

            if (currentDB.exists()) {
                FileInputStream(currentDB).use { input ->
                    FileOutputStream(backupDB).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun importDatabase(): Boolean = withContext(Dispatchers.IO) {
        try {
            val backupDB = File(context.getExternalFilesDir(null), backupFileName)
            val currentDB = context.getDatabasePath(dbName)

            if (backupDB.exists()) {
                FileInputStream(backupDB).use { input ->
                    FileOutputStream(currentDB).use { output ->
                        input.copyTo(output)
                    }
                }

                // Close the existing database connection
                AppDatabase.getDatabase(context).close()

                // Reopen the database
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    dbName
                ).build()

                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
