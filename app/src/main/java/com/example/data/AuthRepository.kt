package com.example.data

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.example.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await
import android.util.Log

class AuthRepository(private val context: Context) {
    private val auth = Firebase.auth
    val currentUser = auth.currentUser

    suspend fun signInWithGoogle(): Boolean {
        try {
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.FIREBASE_WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            
            if (credential is GoogleIdTokenCredential) {
                val idToken = credential.idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCredential).await()
                return true
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign in failed", e)
        }
        return false
    }

    fun signOut() {
        auth.signOut()
    }
}
