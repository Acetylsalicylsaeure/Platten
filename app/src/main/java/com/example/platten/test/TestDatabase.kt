package com.example.platten.test

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Entity
data class TestEntity(@PrimaryKey val id: Int, val name: String)

@Dao
interface TestDao {
    @Insert
    fun insert(entity: TestEntity)

    @Query("SELECT * FROM TestEntity")
    fun getAll(): List<TestEntity>
}

@Database(entities = [TestEntity::class], version = 1, exportSchema = false)
abstract class TestDatabase : RoomDatabase() {
    abstract fun testDao(): TestDao
}
