package com.hwaranga.flashback

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class PhotoViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_view)

        val photoPath = intent.getStringExtra("photo_path") ?: return

        val imageView = findViewById<ImageView>(R.id.fullImageView)
        val backButton = findViewById<ImageButton>(R.id.backButton)

        Glide.with(this)
            .load(photoPath)
            .into(imageView)

        backButton.setOnClickListener {
            finish()
        }
    }
}