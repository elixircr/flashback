package com.hwaranga.flashback

// imports
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.widget.PopupMenu
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.LinearLayout
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.widget.ImageButton

class MainActivity : AppCompatActivity() {


    // variables

    //lateinit initialises variables later

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: PreviewView

    private lateinit var cameraButton: ImageButton

    private lateinit var switchCamButton: ImageButton

    private lateinit var galleryButton: ImageButton

    // means that this var is nullable
    private var imageCapture: ImageCapture? = null

    private var cameraProvider: ProcessCameraProvider? = null

    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    private var selectedExpiry = 30




    // static class holder for variables
    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
    }

    // pretty much same as a void start func; runs when script is first created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // sets the UI as vars using the set ID's
        previewView = findViewById(R.id.previewView)
        cameraButton = findViewById(R.id.cameraButton)
        switchCamButton = findViewById(R.id.switchCamButton)
        galleryButton = findViewById(R.id.galleryButton)

        val expiryLabel = findViewById<TextView>(R.id.expiryLabel)
        val expiryContainer = findViewById<LinearLayout>(R.id.expiryContainer)

        cameraExecutor = Executors.newSingleThreadExecutor()

        deleteExpiredPhotos()

        if (hasCameraPermission()) {
            startCamera()
        } else {
            requestCameraPermission()
        }

        // runs if button was pressed
        cameraButton.setOnClickListener {
            takePhoto()
        }

        switchCamButton.setOnClickListener {
            switchCam()
        }

        galleryButton.setOnClickListener {
            startActivity(Intent(this, GalleryActivity::class.java))
        }



        // runs when expiry date change button is clicked
        expiryContainer.setOnClickListener {
            val popup = PopupMenu(this, expiryContainer)

            popup.menu.add(0, 1, 0, "1 day")
            popup.menu.add(0, 5, 0, "5 days")
            popup.menu.add(0, 30, 0, "30 days")

            popup.show()

            // runs when user selects a date
            popup.setOnMenuItemClickListener { item ->
                selectedExpiry = item.itemId
                expiryLabel.text = "${selectedExpiry}d"
                true
            }
        }

        // creates notification channel
        createNotificationChannel()

        // requests media reading and notification permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.READ_MEDIA_IMAGES
                ),
                101
            )
        }
    }

    // returns bool as true if permission has been given already
    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    //  requests for camera permissions
    private fun requestCameraPermission() {
        // requests for camera perms
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
    }

    // OnRequestPermissionsResult runs after user responds to a permission dialog
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    // start camera func; begins the camera on its own thread and binds it when ready
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCamera()
        }, ContextCompat.getMainExecutor(this))
    }

    // binds camera to previewview ui so that the user can see the camera activity
    private fun bindCamera() {
        val cameraProvider = cameraProvider ?: return

        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }

        imageCapture = ImageCapture.Builder().build()

        // clears all the previous bindings before applying new ones to not get any bugs
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
        } catch(e: Exception) {
            Toast.makeText(this, "Camera failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // take photo func; gets the date of current time using the sdf and sets it to the name of the photo, along with the expiry date. saves it to app's internal storage
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val sdf = SimpleDateFormat("ddMMyy_HHmmss", Locale.getDefault())
        val dateString = sdf.format(Date())

        // CHANGED: Using filesDir instead of externalMediaDirs
        val photoFile = File(filesDir, "flashback_${dateString}_${selectedExpiry}d.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
            override fun onError(exception: ImageCaptureException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Capture failed ${exception.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Photo taken.", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    // switch camera function; switches camera
    private fun switchCam() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
            CameraSelector.DEFAULT_FRONT_CAMERA
        else
            CameraSelector.DEFAULT_BACK_CAMERA

        bindCamera()
    }

    // closes camera when app closes so it doesnt run in background
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    // file auto deletion func;
    private fun deleteExpiredPhotos() {

        val dir = filesDir ?: return
        val sdf = SimpleDateFormat("ddMMyy_HHmmss", Locale.getDefault())
        val now = Date()

        dir.listFiles { file ->
            file.name.startsWith("flashback_") && file.name.endsWith(".jpg")
        }?.forEach { file ->
            try {
                val parts = file.nameWithoutExtension.split("_")
                //e.g. how the parts are split would be parts[0] = "flashback", parts[1] = "230426", parts[2] = "143022", parts[3] = "30d"
                val datePart = "${parts[1]}_${parts[2]}"
                val expiryDays = parts[3].removeSuffix("d").toInt()

                // changes datePart back to date format
                val takenDate = sdf.parse(datePart) ?: return@forEach
                val expiryMs = expiryDays * 24 * 60 * 60 * 1000L
                val expiryDate = Date(takenDate.time + expiryMs)

                if (now.after(expiryDate)) {
                    file.delete()
                }
            } catch (e: Exception) { }
        }
    }

    // creates notification channel which is needed to send notifications
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "flashback_channel",
            "Flashback Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

}