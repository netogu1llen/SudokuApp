package com.sudokuapp.data.model


import com.google.gson.annotations.SerializedName

data class SudokuApiResponse(
    @SerializedName("puzzle") val puzzle: List<List<Int?>>,
    @SerializedName("solution") val solution: List<List<Int>>
)


