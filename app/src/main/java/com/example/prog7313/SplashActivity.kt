package com.example.prog7313

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var imgSplashLogo: ImageView
    private lateinit var tvSplashName: TextView
    private lateinit var tvSplashTagline: TextView
    private lateinit var loadingBar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        imgSplashLogo = findViewById(R.id.imgSplashLogo)
        tvSplashName = findViewById(R.id.tvSplashName)
        tvSplashTagline = findViewById(R.id.tvSplashTagline)
        loadingBar = findViewById(R.id.loadingBar)

        animateSplash()

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
        }, 2300)
    }

    private fun animateSplash() {
        imgSplashLogo.alpha = 0f
        imgSplashLogo.scaleX = 0.75f
        imgSplashLogo.scaleY = 0.75f

        tvSplashName.alpha = 0f
        tvSplashTagline.alpha = 0f
        loadingBar.scaleX = 0f

        imgSplashLogo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(700)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        tvSplashName.animate()
            .alpha(1f)
            .translationY(-8f)
            .setStartDelay(450)
            .setDuration(500)
            .start()

        tvSplashTagline.animate()
            .alpha(1f)
            .translationY(-6f)
            .setStartDelay(700)
            .setDuration(500)
            .start()

        loadingBar.animate()
            .scaleX(1f)
            .setStartDelay(900)
            .setDuration(900)
            .start()
    }
}