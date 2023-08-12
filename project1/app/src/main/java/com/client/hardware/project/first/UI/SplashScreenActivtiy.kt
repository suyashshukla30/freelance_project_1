package com.client.hardware.project.first.UI

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.client.hardware.project.first.R

class SplashScreenActivtiy : AppCompatActivity() {
    private val SPLASH_DELAY: Long = 5000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen_activtiy)
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginAdmin::class.java))
            finish()
        },SPLASH_DELAY)
    }
}