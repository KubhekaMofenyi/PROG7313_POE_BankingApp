package com.example.prog7313

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ReceiptViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt_viewer)

        val imageView = findViewById<ImageView>(R.id.ivReceipt)
        val uriString = intent.getStringExtra("receiptUri")

        if (uriString.isNullOrEmpty()) {
            Toast.makeText(this, "No receipt found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        try {
            imageView.setImageURI(Uri.parse(uriString))
        } catch (e: Exception) {
            Toast.makeText(this, "Could not open receipt", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}