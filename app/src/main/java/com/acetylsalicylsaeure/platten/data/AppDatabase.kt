package com.acetylsalicylsaeure.platten.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Exercise::class,
        ExerciseLog::class,
        Workout::class,
        WorkoutExerciseCrossRef::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun exerciseLogDao(): ExerciseLogDao
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE exercises ADD COLUMN weight_steps FLOAT NOT NULL DEFAULT 0.0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE exercises ADD COLUMN hidden INTEGER NOT NULL DEFAULT 0")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_exercise_logs_exerciseId ON exercise_logs(exerciseId)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create workouts table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS workouts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        lastViewed INTEGER NOT NULL
                    )
                """)

                // Create workout-exercise cross-reference table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS workout_exercise_cross_ref (
                        workoutId INTEGER NOT NULL,
                        exerciseId INTEGER NOT NULL,
                        orderPosition INTEGER NOT NULL,
                        PRIMARY KEY(workoutId, exerciseId),
                        FOREIGN KEY(workoutId) REFERENCES workouts(id) ON DELETE CASCADE,
                        FOREIGN KEY(exerciseId) REFERENCES exercises(id) ON DELETE CASCADE
                    )
                """)

                // Create indices for better query performance
                database.execSQL("CREATE INDEX IF NOT EXISTS index_workout_exercise_cross_ref_workoutId ON workout_exercise_cross_ref(workoutId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_workout_exercise_cross_ref_exerciseId ON workout_exercise_cross_ref(exerciseId)")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create new table with the new schema
                database.execSQL("""
                    CREATE TABLE workouts_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL DEFAULT 'Workout',
                        lastViewed INTEGER NOT NULL
                    )
                """)

                // Copy data from old table to new table
                database.execSQL("""
                    INSERT INTO workouts_new (id, lastViewed)
                    SELECT id, lastViewed FROM workouts
                """)

                // Remove old table
                database.execSQL("DROP TABLE workouts")

                // Rename new table to original name
                database.execSQL("ALTER TABLE workouts_new RENAME TO workouts")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "platten_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .addCallback(DatabaseInitializer(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}