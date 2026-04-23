package com.example.prog7313

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val btnSendReset = findViewById<Button>(R.id.btnSendReset)
        val tvResetMessage = findViewById<TextView>(R.id.tvResetMessage)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)

        btnSendReset.setOnClickListener {
            tvResetMessage.visibility = View.VISIBLE
        }

        tvBackToLogin.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}