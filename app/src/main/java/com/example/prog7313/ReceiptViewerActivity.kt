package com.example.prog7313

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class ReceiptViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt_viewer)

        val imageView = findViewById<ImageView>(R.id.ivReceipt)

        val uriString = intent.getStringExtra("receiptUri")

        if (!uriString.isNullOrEmpty()) {
            imageView.setImageURI(Uri.parse(uriString))
        }
    }
}