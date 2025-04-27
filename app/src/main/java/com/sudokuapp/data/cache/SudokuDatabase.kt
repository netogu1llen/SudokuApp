package com.sudokuapp.data.cache

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sudokuapp.data.cache.dao.SudokuDao
import com.sudokuapp.data.cache.entity.SudokuConverters
import com.sudokuapp.data.cache.entity.SudokuEntity


@Database(entities = [SudokuEntity::class], version = 1, exportSchema = false)
@TypeConverters(SudokuConverters::class)
abstract class SudokuDatabase : RoomDatabase() {

    abstract fun sudokuDao(): SudokuDao

    companion object {
        @Volatile
        private var INSTANCE: SudokuDatabase? = null

        fun getDatabase(context: Context): SudokuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SudokuDatabase::class.java,
                    "sudoku_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}