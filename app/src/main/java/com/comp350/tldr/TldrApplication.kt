package com.comp350.tldr

import android.app.Application
import com.google.firebase.FirebaseApp

class TldrApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
    }
}