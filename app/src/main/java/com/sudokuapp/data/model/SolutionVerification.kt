package com.sudokuapp.data.model

import com.google.gson.annotations.SerializedName

data class SudokuVerificationRequest(
    @SerializedName("board") val board: List<List<Int?>>
)

data class SudokuVerificationResponse(
    @SerializedName("solution") val solution: List<List<Int>>,
    @SerializedName("solvable") val solvable: Boolean,
    @SerializedName("difficulty") val difficulty: String?
)