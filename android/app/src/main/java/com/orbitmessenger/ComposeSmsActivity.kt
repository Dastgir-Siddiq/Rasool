package com.orbitmessenger

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class ComposeSmsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Forward to MainActivity to handle the intent
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            action = intent.action
            data = intent.data
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(mainIntent)
        finish()
    }
}
