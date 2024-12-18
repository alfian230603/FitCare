package com.app.fitcare.models

data class DailyLog(
    val date: String,
    var waterIntake: Int,
    var stepCount: Int
)
