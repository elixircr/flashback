package com.hwaranga.flashback

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.io.File

class PhotoViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_view)

        // retrieve the file path that was passed from GalleryActivity
        val photoPath = intent.getStringExtra("photo_path") ?: return

        val imageView = findViewById<ImageView>(R.id.fullImageView)
        val backButton = findViewById<ImageButton>(R.id.backButton)

        val deleteButton = findViewById<Button>(R.id.deleteButton)

        deleteButton.setOnClickListener {
            File(photoPath).delete()
            finish()
        }

        Glide.with(this)
            .load(photoPath)
            .into(imageView)

        backButton.setOnClickListener {
            finish()
        }
    }
}