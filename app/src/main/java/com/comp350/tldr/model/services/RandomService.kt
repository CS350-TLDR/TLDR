package com.comp350.tldr.model.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class RandomService : Service() {
    private val serviceIdentifier = "RandomService"
    private var timer: Timer? = null
    private var intervalMs: Long = 60000
    private var currentActiveService: String? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPrefs: android.content.SharedPreferences

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        auth = FirebaseAuth.getInstance()
        sharedPrefs = getSharedPreferences("tldr_prefs", Context.MODE_PRIVATE)
        Log.d(serviceIdentifier, "RandomService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent ?: return START_NOT_STICKY

        when (intent.action) {
            "START_SERVICE" -> handleStart(intent)
            "STOP_SERVICE" -> stopSelf()
            "SHOW_NOW" -> showRandomActivity()
        }

        return START_NOT_STICKY
    }

    private fun handleStart(intent: Intent) {
        intervalMs = intent.getLongExtra("interval", 60000)

        timer?.cancel()
        timer = Timer()

        // Show first activity immediately
        timer?.schedule(object : TimerTask() {
            override fun run() {
                Handler(Looper.getMainLooper()).post {
                    try {
                        showRandomActivity()
                    } catch (e: Exception) {
                        Log.e(serviceIdentifier, "Error showing initial activity", e)
                    }
                }
            }
        }, 0)

        // Schedule recurring activities
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                Handler(Looper.getMainLooper()).post {
                    try {
                        showRandomActivity()
                    } catch (e: Exception) {
                        Log.e(serviceIdentifier, "Error showing scheduled activity", e)
                    }
                }
            }
        }, intervalMs, intervalMs)

        Toast.makeText(this, "Random activities activated", Toast.LENGTH_SHORT).show()
    }

    private fun showRandomActivity() {
        // Choose a random activity from the three available
        val activities = listOf("Trivia", "Video", "VocabMatch")
        val randomActivity = activities.random()

        // First stop the currently active service if any
        stopCurrentService()

        // Track the newly started service
        currentActiveService = randomActivity

        // Start the randomly chosen service with START_SERVICE instead of SHOW_NOW
        when (randomActivity) {
            "Trivia" -> {
                Log.d(serviceIdentifier, "Starting Trivia activity")
                val intent = Intent(this, TriviaService::class.java).apply {
                    action = "START_SERVICE"
                    putExtra("interval", 100000000L) // Set a very long interval so it doesn't auto-refresh
                }
                startService(intent)
            }
            "Video" -> {
                Log.d(serviceIdentifier, "Starting Video activity")
                val intent = Intent(this, VideoService::class.java).apply {
                    action = "START_SERVICE"
                    putExtra("interval", 100000000L)
                }
                startService(intent)
            }
            "VocabMatch" -> {
                Log.d(serviceIdentifier, "Starting VocabMatch activity")
                val intent = Intent(this, VocabMatchService::class.java).apply {
                    action = "START_SERVICE"
                    putExtra("interval", 100000000L)
                }
                startService(intent)
            }
        }
    }

    private fun stopCurrentService() {
        // Only stop the currently active service
        when (currentActiveService) {
            "Trivia" -> {
                val intent = Intent(this, TriviaService::class.java).apply {
                    action = "STOP_SERVICE"
                }
                startService(intent)
            }
            "Video" -> {
                val intent = Intent(this, VideoService::class.java).apply {
                    action = "STOP_SERVICE"
                }
                startService(intent)
            }
            "VocabMatch" -> {
                val intent = Intent(this, VocabMatchService::class.java).apply {
                    action = "STOP_SERVICE"
                }
                startService(intent)
            }
        }
    }

    override fun onDestroy() {
        timer?.cancel()
        // Stop any active service when this service is destroyed
        stopCurrentService()
        Log.d(serviceIdentifier, "RandomService destroyed")
        super.onDestroy()
    }
}