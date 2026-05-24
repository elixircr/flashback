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

    // runs at start of script
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        recyclerView = findViewById(R.id.galleryRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 3)

        // loads only photos taken by this app
        val photos = getAppPhotos()
        recyclerView.adapter = GalleryAdapter(photos)

        // ends activity when back button is pressed
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    // reloads gallery every time gallery gets active
    override fun onResume() {
        super.onResume()
        val photos = getAppPhotos()
        recyclerView.adapter = GalleryAdapter(photos)
    }

    // function that sets all the photos from the app into a list
    private fun getAppPhotos(): List<File> {
        val dir = filesDir ?: return emptyList()
        return dir.listFiles { file ->
            file.name.startsWith("flashback_") && file.name.endsWith(".jpg")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    // Adapter manages how the iamges show on the grid
    class GalleryAdapter(private val photos: List<File>) :
        RecyclerView.Adapter<GalleryAdapter.PhotoViewHolder>() {

        // viewholder caches each grid item so it deloads when scrolling past
        class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(R.id.photoImageView)
            val expiryBadge: TextView = view.findViewById(R.id.expiryBadge)
        }

        // inflates item_photo xml layout and wraps it in a viewholder
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_photo, parent, false)
            return PhotoViewHolder(view)
        }

        // binds a photo to a grid tile and loads all the ui
        override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
            val photo = photos[position]

            // loads images
            Glide.with(holder.imageView.context)
                .load(photo)
                .centerCrop()
                .into(holder.imageView)

            // parses days remaining and shows days left
            try {
                val parts = photo.nameWithoutExtension.split("_")
                val datePart = "${parts[1]}_${parts[2]}"
                val expiryDays = parts[3].removeSuffix("d").toInt()

                val sdf = SimpleDateFormat("ddMMyy_HHmmss", Locale.getDefault())
                val takenDate = sdf.parse(datePart)!!
                val expiryMs = expiryDays * 24 * 60 * 60 * 1000L
                val expiryDate = Date(takenDate.time + expiryMs)

                // calculations
                val daysLeft = ((expiryDate.time - Date().time + (1000 * 60 * 60 * 24 - 1)) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)

                // displays badge next to image in grid
                holder.expiryBadge.text = "${daysLeft}d"
            } catch (e: Exception) {
                holder.expiryBadge.text = ""
            }

            // opens photo view when image is pressed; sends filepath through intent
            holder.imageView.setOnClickListener {
                val intent = Intent(holder.imageView.context, PhotoViewActivity::class.java)
                intent.putExtra("photo_path", photo.absolutePath)
                holder.imageView.context.startActivity(intent)
            }
        }

        // counts how many photos tehre are
        override fun getItemCount() = photos.size
    }

}