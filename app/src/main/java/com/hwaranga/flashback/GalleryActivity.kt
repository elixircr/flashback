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

class GalleryActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView

    // start function
    override fun onCreate(savedInstanceState: Bundle?) {
        // needed things
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        // sets var to view widget set in activity_gallery.xml
        recyclerView = findViewById(R.id.galleryRecyclerView)
        // sets recyclerview to grid with 3 rows
        recyclerView.layoutManager = GridLayoutManager(this, 3)

        // loads only photos taken by this app
        val photos = getAppPhotos()
        // adapter sets list into ui
        recyclerView.adapter = GalleryAdapter(photos)

        // back button
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish() // finish func closes current activity
        }
    }

    // func
    private fun getAppPhotos(): List<File> {
        // gets all the jpg files starting with flashback_, sorted by the most recently taken as a list
        return filesDir.listFiles { file ->
            file.name.startsWith("flashback_") && file.name.endsWith(".jpg")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    // gets list of the photos in new class and sends to adapter ???
    class GalleryAdapter(private val photos: List<File>) :
        RecyclerView.Adapter<GalleryAdapter.PhotoViewHolder>() {

            // sets viewholder to a .xml file so it doesnt get called for every photo
        class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(R.id.photoImageView)
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

            // open full screen view when tile is tapped
            holder.imageView.setOnClickListener {
                val intent = Intent(holder.imageView.context, PhotoViewActivity::class.java)
                intent.putExtra("photo_path", photo.absolutePath)
                holder.imageView.context.startActivity(intent)
            }
        }

        override fun getItemCount() = photos.size
    }
}