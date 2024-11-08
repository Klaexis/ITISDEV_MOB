package com.itisdev.application

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        Log.d("MyApplication", "FirebaseApp initialized")

        Firebase.messaging.subscribeToTopic("announcements")
            .addOnCompleteListener { task ->
                var msg = "Subscribed to announcements"
                if (!task.isSuccessful) {
                    msg = "Subscription failed"
                }
                Log.d("MyApplication", msg)
            }
    }
}