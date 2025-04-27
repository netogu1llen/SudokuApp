package com.sudokuapp.data.api

import com.sudokuapp.data.model.SudokuApiResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SudokuApiService {
    @GET("v1/sudoku")
    suspend fun generateSudoku(
        @Header("X-Api-Key") apiKey: String,
        @Query("difficulty") difficulty: String,
        @Query("width") width: Int,
        @Query("height") height: Int
    ): SudokuApiResponse
}