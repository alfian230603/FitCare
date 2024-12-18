package com.app.fitcare.models

data class WaterLog(
    var userId: String = "",
    var date: String = "",
    var waterIntake: Int = 0,
    var createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val COLLECTION_NAME = "WaterLogs"
    }
}
