package com.app.fitcare.models

data class StepLog(
    var userId: String = "",
    var date: String = "",
    var stepCount: Int = 0,
    var createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val COLLECTION_NAME = "StepLogs"
    }
}
