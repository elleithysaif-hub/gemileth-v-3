package com.example

import android.app.Application
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class GemileithApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            val options = FirebaseOptions.Builder()
                .setApiKey(BuildConfig.FIREBASE_API_KEY)
                .setApplicationId(BuildConfig.FIREBASE_APP_ID)
                .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
                .build()
            
            FirebaseApp.initializeApp(this, options)
        } catch (e: Exception) {
            Log.e("GemileithApp", "Error initializing Firebase", e)
        }
    }
}
