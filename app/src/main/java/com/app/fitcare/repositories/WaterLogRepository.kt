package com.app.fitcare.repositories

import com.app.fitcare.models.WaterLog
import com.google.firebase.firestore.FirebaseFirestore

class WaterLogRepository {

    private val db = FirebaseFirestore.getInstance()

    fun create(waterLog: WaterLog, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(WaterLog.COLLECTION_NAME)
            .add(waterLog)
            .addOnSuccessListener { document ->
                onSuccess(document.id)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun readAll(onSuccess: (List<Pair<String, WaterLog>>) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(WaterLog.COLLECTION_NAME)
            .get()
            .addOnSuccessListener { result ->
                val waterLogs = result.map { document ->
                    document.id to document.toObject(WaterLog::class.java)
                }
                onSuccess(waterLogs)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun readById(logId: String, onSuccess: (Pair<String, WaterLog>?) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(WaterLog.COLLECTION_NAME).document(logId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val waterLog = document.toObject(WaterLog::class.java)
                    onSuccess(document.id to waterLog!!)
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun readByUserId(userId: String, onSuccess: (List<Pair<String, WaterLog>>) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(WaterLog.COLLECTION_NAME)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val waterLogs = result.map { document ->
                    document.id to document.toObject(WaterLog::class.java)
                }
                onSuccess(waterLogs)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun update(logId: String, updatedData: Map<String, Any>, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(WaterLog.COLLECTION_NAME).document(logId)
            .update(updatedData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun delete(logId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(WaterLog.COLLECTION_NAME).document(logId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}
