package com.example.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar

data class FirestoreMessage(
    val text: String = "",
    val isUser: Boolean = false,
    val timestamp: Long = 0L
)

class FirestoreRepository {
    private val db = Firebase.firestore

    private fun getCurrentTimeOfDay(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "MORNING"
            in 12..16 -> "AFTERNOON"
            in 17..20 -> "EVENING"
            else -> "NIGHT"
        }
    }

    suspend fun saveAppLaunch(userId: String, packageName: String) {
        val launch = hashMapOf(
            "packageName" to packageName,
            "timestamp" to System.currentTimeMillis(),
            "timeOfDay" to getCurrentTimeOfDay()
        )
        try {
            db.collection("users")
                .document(userId)
                .collection("appLaunches")
                .add(launch)
                .await()
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error saving app launch", e)
        }
    }

    suspend fun getSuggestedApps(userId: String): List<String> {
        val timeOfDay = getCurrentTimeOfDay()
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("appLaunches")
                .whereEqualTo("timeOfDay", timeOfDay)
                .get()
                .await()
            val counts = snapshot.documents.groupingBy { it.getString("packageName") ?: "" }.eachCount()
            counts.entries.sortedByDescending { it.value }.take(4).map { it.key }.filter { it.isNotBlank() }
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error getting suggested apps", e)
            emptyList()
        }
    }

    suspend fun saveMessage(userId: String, text: String, isUser: Boolean) {
        val message = FirestoreMessage(text, isUser, System.currentTimeMillis())
        try {
            db.collection("users")
                .document(userId)
                .collection("messages")
                .add(message)
                .await()
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error saving message", e)
        }
    }

    suspend fun getMessages(userId: String): List<FirestoreMessage> {
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()
            snapshot.toObjects(FirestoreMessage::class.java)
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error getting messages", e)
            emptyList()
        }
    }

    suspend fun clearMessages(userId: String) {
        try {
            val messages = db.collection("users")
                .document(userId)
                .collection("messages")
                .get()
                .await()
            for (document in messages.documents) {
                document.reference.delete().await()
            }
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error clearing messages", e)
        }
    }
}
