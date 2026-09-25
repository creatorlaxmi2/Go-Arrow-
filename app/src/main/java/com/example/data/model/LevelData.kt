package com.example.data.model

data class LevelData(
    val id: Int,
    val title: String = "Level $id",
    val difficulty: String = "Normal", // Easy, Normal, Hard, Expert
    val cols: Int = 10,
    val rows: Int = 14,
    val arrows: List<ArrowModel>
)
