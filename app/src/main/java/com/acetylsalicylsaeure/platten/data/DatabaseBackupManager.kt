package com.acetylsalicylsaeure.platten.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

class DatabaseBackupManager(private val context: Context) {
    private val database: AppDatabase = AppDatabase.getDatabase(context)

    suspend fun backupDatabase(destination: Any) {
        when (destination) {
            is Uri -> backupToUri(destination)
            is File -> backupToFile(destination)
            else -> throw IllegalArgumentException("Unsupported destination type")
        }
    }

    suspend fun restoreDatabase(source: Any) {
        when (source) {
            is Uri -> restoreFromUri(source)
            is File -> restoreFromFile(source)
            else -> throw IllegalArgumentException("Unsupported source type")
        }
    }

    private suspend fun backupToUri(uri: Uri) {
        val backupFolder = DocumentFile.fromTreeUri(context, uri)
            ?.createDirectory("Platten_Backup_${getCurrentDate()}")

        if (backupFolder != null && backupFolder.exists()) {
            val exercises = database.exerciseDao().getAllExercisesSync()
            val logs = database.exerciseLogDao().getAllLogsSync()

            saveToCSV(backupFolder, "exercises.csv", exercises) { exercise ->
                "${exercise.id},${exercise.name},${exercise.weightSteps}"
            }

            saveToCSV(backupFolder, "logs.csv", logs) { log ->
                "${log.id},${log.exerciseId},${log.date.time},${log.weight},${log.reps}"
            }
        }
    }

    private suspend fun backupToFile(directory: File) {
        val exercisesFile = File(directory, "exercises.csv")
        val logsFile = File(directory, "logs.csv")

        exercisesFile.bufferedWriter().use { writer ->
            database.exerciseDao().getAllExercisesSync().forEach { exercise ->
                writer.write("${exercise.id},${exercise.name},${exercise.weightSteps}\n")
            }
        }

        logsFile.bufferedWriter().use { writer ->
            database.exerciseLogDao().getAllLogsSync().forEach { log ->
                writer.write("${log.id},${log.exerciseId},${log.date.time},${log.weight},${log.reps}\n")
            }
        }
    }

    private suspend fun restoreFromUri(uri: Uri) {
        val backupFolder = DocumentFile.fromTreeUri(context, uri)

        if (backupFolder != null && backupFolder.exists()) {
            val exercisesFile = backupFolder.findFile("exercises.csv")
            val logsFile = backupFolder.findFile("logs.csv")

            if (exercisesFile != null && logsFile != null) {
                database.exerciseDao().deleteAllExercises()
                database.exerciseLogDao().deleteAllLogs()

                restoreExercisesFromInputStream(context.contentResolver.openInputStream(exercisesFile.uri))
                restoreLogsFromInputStream(context.contentResolver.openInputStream(logsFile.uri))
            }
        }
    }

    private suspend fun restoreFromFile(directory: File) {
        val exercisesFile = File(directory, "exercises.csv")
        val logsFile = File(directory, "logs.csv")

        if (exercisesFile.exists() && logsFile.exists()) {
            database.exerciseDao().deleteAllExercises()
            database.exerciseLogDao().deleteAllLogs()

            restoreExercisesFromInputStream(exercisesFile.inputStream())
            restoreLogsFromInputStream(logsFile.inputStream())
        }
    }

    private suspend fun restoreExercisesFromInputStream(inputStream: InputStream?) {
        inputStream?.bufferedReader()?.useLines { lines ->
            lines.forEach { line ->
                val parts = line.split(",")
                if (parts.size == 3) {
                    val exercise = Exercise(
                        id = parts[0].toInt(),
                        name = parts[1],
                        weightSteps = parts[2].toDouble()
                    )
                    database.exerciseDao().insertExercise(exercise)
                }
            }
        }
    }

    private suspend fun restoreLogsFromInputStream(inputStream: InputStream?) {
        inputStream?.bufferedReader()?.useLines { lines ->
            lines.forEach { line ->
                val parts = line.split(",")
                if (parts.size == 5) {
                    val log = ExerciseLog(
                        id = parts[0].toInt(),
                        exerciseId = parts[1].toInt(),
                        date = Date(parts[2].toLong()),
                        weight = parts[3].toFloat(),
                        reps = parts[4].toInt()
                    )
                    database.exerciseLogDao().insertLog(log)
                }
            }
        }
    }

    private fun <T> saveToCSV(folder: DocumentFile, fileName: String, data: List<T>, transform: (T) -> String) {
        val file = folder.createFile("text/csv", fileName)
        if (file != null) {
            context.contentResolver.openOutputStream(file.uri)?.use { outputStream ->
                outputStream.bufferedWriter().use { writer ->
                    data.forEach { item ->
                        writer.write(transform(item))
                        writer.newLine()
                    }
                }
            }
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", java.util.Locale.getDefault())
        return dateFormat.format(Date())
    }
}