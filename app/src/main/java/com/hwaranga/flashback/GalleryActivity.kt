package com.hwaranga.flashback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File

class GalleryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        recyclerView = findViewById(R.id.galleryRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 3)

        // loads only photos taken by this app
        val photos = getAppPhotos()
        recyclerView.adapter = GalleryAdapter(photos)

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish() // closes this Activity and goes back, like popping a scene in Unity
        }
    }

    private fun getAppPhotos(): List<File> {
        val dir = externalMediaDirs.firstOrNull() ?: return emptyList()
        return dir.listFiles { file ->
            file.name.startsWith("flashback_") && file.name.endsWith(".jpg")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    // Adapter — like a script that manages a list of UI elements
    class GalleryAdapter(private val photos: List<File>) :
        RecyclerView.Adapter<GalleryAdapter.PhotoViewHolder>() {

        class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(R.id.photoImageView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_photo, parent, false)
            return PhotoViewHolder(view)
        }

        override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
            Glide.with(holder.imageView.context)
                .load(photos[position])
                .centerCrop()
                .into(holder.imageView)
        }

        override fun getItemCount() = photos.size
    }
}