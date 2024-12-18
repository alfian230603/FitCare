package com.app.fitcare.models

data class User(
    var name: String = "",
    var username: String = "",
    var email: String = "",
    var phone: String = "",
    var password: String = "",
    var targetWater: Int = 2000,
    var targetSteps: Int = 5000,
    var createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val COLLECTION_NAME = "Users"
    }
}
