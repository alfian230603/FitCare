package com.app.fitcare.repositories

import com.app.fitcare.models.StepLog
import com.google.firebase.firestore.FirebaseFirestore

class StepLogRepository {

    private val db = FirebaseFirestore.getInstance()

    fun create(stepLog: StepLog, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(StepLog.COLLECTION_NAME)
            .add(stepLog)
            .addOnSuccessListener { document ->
                onSuccess(document.id)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun readAll(onSuccess: (List<Pair<String, StepLog>>) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(StepLog.COLLECTION_NAME)
            .get()
            .addOnSuccessListener { result ->
                val stepLogs = result.map { document ->
                    document.id to document.toObject(StepLog::class.java)
                }
                onSuccess(stepLogs)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun readById(logId: String, onSuccess: (Pair<String, StepLog>?) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(StepLog.COLLECTION_NAME).document(logId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val stepLog = document.toObject(StepLog::class.java)
                    onSuccess(document.id to stepLog!!)
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun readByUserId(userId: String, onSuccess: (List<Pair<String, StepLog>>) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(StepLog.COLLECTION_NAME)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val stepLogs = result.map { document ->
                    document.id to document.toObject(StepLog::class.java)
                }
                onSuccess(stepLogs)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun update(logId: String, updatedData: Map<String, Any>, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(StepLog.COLLECTION_NAME).document(logId)
            .update(updatedData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun delete(logId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(StepLog.COLLECTION_NAME).document(logId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}
