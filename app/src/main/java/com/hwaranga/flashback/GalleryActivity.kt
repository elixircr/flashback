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
import android.content.Intent
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        // CHANGED: Targeting filesDir directly instead of externalMediaDirs
        val dir = filesDir ?: return emptyList()
        return dir.listFiles { file ->
            file.name.startsWith("flashback_") && file.name.endsWith(".jpg")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    // Adapter — like a script that manages a list of UI elements
    class GalleryAdapter(private val photos: List<File>) :
        RecyclerView.Adapter<GalleryAdapter.PhotoViewHolder>() {

        class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(R.id.photoImageView)
            val expiryBadge: TextView = view.findViewById(R.id.expiryBadge)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_photo, parent, false)
            return PhotoViewHolder(view)
        }

        override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
            val photo = photos[position]

            Glide.with(holder.imageView.context)
                .load(photo)
                .centerCrop()
                .into(holder.imageView)

            // parse days remaining from filename
            try {
                val parts = photo.nameWithoutExtension.split("_")
                val datePart = "${parts[1]}_${parts[2]}"
                val expiryDays = parts[3].removeSuffix("d").toInt()

                val sdf = SimpleDateFormat("ddMMyy_HHmmss", Locale.getDefault())
                val takenDate = sdf.parse(datePart)!!
                val expiryMs = expiryDays * 24 * 60 * 60 * 1000L
                val expiryDate = Date(takenDate.time + expiryMs)

                val daysLeft = ((expiryDate.time - Date().time) / (1000 * 60 * 60 * 24)).toInt()
                    .coerceAtLeast(0)

                holder.expiryBadge.text = "${daysLeft}d"
            } catch (e: Exception) {
                holder.expiryBadge.text = ""
            }

            holder.imageView.setOnClickListener {
                val intent = Intent(holder.imageView.context, PhotoViewActivity::class.java)
                intent.putExtra("photo_path", photo.absolutePath)
                holder.imageView.context.startActivity(intent)
            }
        }

        override fun getItemCount() = photos.size
    }
}