package com.app.fitcare.repositories

import com.app.fitcare.models.User
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {

    private val db = FirebaseFirestore.getInstance()

    fun register(user: User, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(User.COLLECTION_NAME)
            .whereEqualTo("username", user.username)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    create(user, onSuccess, onFailure)
                } else {
                    onFailure(Exception("Username sudah digunakan"))
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun login(username: String, password: String, onSuccess: (Pair<String, User>?) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(User.COLLECTION_NAME)
            .whereEqualTo("username", username)
            .whereEqualTo("password", password)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val document = result.documents.first()
                    val user = document.toObject(User::class.java)
                    onSuccess(document.id to user!!)
                } else {
                    onFailure(Exception("Username atau password salah"))
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun create(user: User, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(User.COLLECTION_NAME)
            .add(user)
            .addOnSuccessListener { document ->
                onSuccess(document.id)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun readAll(onSuccess: (List<Pair<String, User>>) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(User.COLLECTION_NAME)
            .get()
            .addOnSuccessListener { result ->
                val users = result.map { document ->
                    document.id to document.toObject(User::class.java)
                }
                onSuccess(users)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun readById(userId: String, onSuccess: (Pair<String, User>?) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(User.COLLECTION_NAME).document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    onSuccess(document.id to user!!)
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun update(userId: String, updatedData: Map<String, Any>, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(User.COLLECTION_NAME).document(userId)
            .update(updatedData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun delete(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(User.COLLECTION_NAME).document(userId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}
