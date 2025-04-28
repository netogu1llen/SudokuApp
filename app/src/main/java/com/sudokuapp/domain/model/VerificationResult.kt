package com.sudokuapp.domain.model

data class VerificationResult(
    val isValid: Boolean,
    val solution: List<List<Int>>?,
    val errorMessage: String?
)