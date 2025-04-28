package com.sudokuapp.data.api

import com.sudokuapp.data.model.SudokuApiResponse
import com.sudokuapp.data.model.SudokuVerificationRequest
import com.sudokuapp.data.model.SudokuVerificationResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface SudokuApiService {
    @GET("v1/sudokugenerate")
    suspend fun generateSudoku(
        @Header("X-Api-Key") apiKey: String,
        @Query("difficulty") difficulty: String,
        @Query("width") width: Int,
        @Query("height") height: Int
    ): SudokuApiResponse

    @POST("v1/sudokusolve")
    suspend fun verifySudokuSolution(
        @Header("X-Api-Key") apiKey: String,
        @Body request: SudokuVerificationRequest
    ): SudokuVerificationResponse
}