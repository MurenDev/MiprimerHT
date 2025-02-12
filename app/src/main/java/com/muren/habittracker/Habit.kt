package com.muren.habittracker

data class Habit(
    val name: String,
    var isCompleted: Boolean = false,
    val completedDates: MutableList<String> = mutableListOf()
)
