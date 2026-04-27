package com.example.prog7313

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity : AppCompatActivity() {

    private var currentPage = 0

    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var ivIcon: ImageView
    private lateinit var btnSkip: Button
    private lateinit var btnNext: Button

    private lateinit var indicator1: View
    private lateinit var indicator2: View
    private lateinit var indicator3: View

    private val titles = listOf(
        "Welcome to Zentavo",
        "Plan Before You Spend",
        "Stay Consistent"
    )

    private val descriptions = listOf(
        "Your personal budgeting companion for tracking expenses, setting goals, and building better financial habits.",
        "Set a monthly goal and allocate spending limits by category.",
        "Track progress, view insights, and earn rewards for good budgeting habits."
    )

    private val icons = listOf(
        R.drawable.zentavo_logo,
        R.drawable.zentavo_logo,
        R.drawable.zentavo_logo
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        tvTitle = findViewById(R.id.tvOnboardingTitle)
        tvDescription = findViewById(R.id.tvOnboardingDescription)
        ivIcon = findViewById(R.id.ivOnboardingIcon)
        btnSkip = findViewById(R.id.btnSkip)
        btnNext = findViewById(R.id.btnNext)

        indicator1 = findViewById(R.id.indicator1)
        indicator2 = findViewById(R.id.indicator2)
        indicator3 = findViewById(R.id.indicator3)

        updatePage()

        btnSkip.setOnClickListener {
            goToLogin()
        }

        btnNext.setOnClickListener {
            if (currentPage < 2) {
                currentPage++
                updatePage()
            } else {
                goToLogin()
            }
        }
    }

    private fun updatePage() {
        tvTitle.text = titles[currentPage]
        tvDescription.text = descriptions[currentPage]
        ivIcon.setImageResource(icons[currentPage])

        ivIcon.alpha = 0f
        ivIcon.animate()
            .alpha(1f)
            .setDuration(400)
            .start()

        tvTitle.alpha = 0f
        tvTitle.animate()
            .alpha(1f)
            .setDuration(400)
            .start()

        tvDescription.alpha = 0f
        tvDescription.animate()
            .alpha(1f)
            .setDuration(400)
            .start()

        indicator1.setBackgroundResource(
            if (currentPage == 0) R.drawable.bg_indicator_active else R.drawable.bg_indicator_inactive
        )

        indicator2.setBackgroundResource(
            if (currentPage == 1) R.drawable.bg_indicator_active else R.drawable.bg_indicator_inactive
        )

        indicator3.setBackgroundResource(
            if (currentPage == 2) R.drawable.bg_indicator_active else R.drawable.bg_indicator_inactive
        )

        btnNext.text = if (currentPage == 2) "Get Started" else "Next"

        val skipParams = btnSkip.layoutParams as LinearLayout.LayoutParams
        val nextParams = btnNext.layoutParams as LinearLayout.LayoutParams

        if (currentPage == 0) {
            btnSkip.visibility = View.GONE

            nextParams.width = 0
            nextParams.weight = 1f
            nextParams.marginStart = 0
            nextParams.marginEnd = 0

            btnNext.layoutParams = nextParams
        } else {
            btnSkip.visibility = View.VISIBLE

            skipParams.width = 0
            skipParams.weight = 1f
            skipParams.marginEnd = 8

            nextParams.width = 0
            nextParams.weight = 1f
            nextParams.marginStart = 8
            nextParams.marginEnd = 0

            btnSkip.layoutParams = skipParams
            btnNext.layoutParams = nextParams
        }
    }

    private fun goToLogin() {
        ivIcon.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 300)
    }
}