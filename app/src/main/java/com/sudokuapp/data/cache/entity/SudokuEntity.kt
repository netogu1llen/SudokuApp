package com.sudokuapp.data.cache.entity


import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.sudokuapp.domain.model.SudokuDifficulty
import com.sudokuapp.domain.model.SudokuSize
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "sudoku_games")
@TypeConverters(SudokuConverters::class)
data class SudokuEntity(
    @PrimaryKey val id: String,
    val puzzle: List<List<Int?>>,
    val solution: List<List<Int>>,
    val currentState: List<List<Int?>>,
    val size: SudokuSize,
    val difficulty: SudokuDifficulty,
    val timestamp: Long,
    val isCompleted: Boolean
)

class SudokuConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromIntListList(value: List<List<Int?>>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toIntListList(value: String): List<List<Int?>> {
        val type = object : TypeToken<List<List<Int?>>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromIntList(value: List<List<Int>>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toIntList(value: String): List<List<Int>> {
        val type = object : TypeToken<List<List<Int>>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromSudokuSize(size: SudokuSize): String {
        return size.name
    }

    @TypeConverter
    fun toSudokuSize(value: String): SudokuSize {
        return SudokuSize.valueOf(value)
    }

    @TypeConverter
    fun fromSudokuDifficulty(difficulty: SudokuDifficulty): String {
        return difficulty.name
    }

    @TypeConverter
    fun toSudokuDifficulty(value: String): SudokuDifficulty {
        return SudokuDifficulty.valueOf(value)
    }
}