package com.hwaranga.flashback

import android.Manifest
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

class MainActivity : AppCompatActivity() {


    // variables

    // lateinit means not defined yet but will be later
    private lateinit var previewView: PreviewView

    private lateinit var cameraButton: Button

    private lateinit var cameraExecutor: ExecutorService

    private lateinit var switchCamButton: Button

    // means that this var is nullable
    private var imageCapture: ImageCapture? = null

    private var cameraProvider: ProcessCameraProvider? = null

    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA




    // sets companion object func can be seen as a static class holder for variables
    companion object {
        // const val means that this value will be hardcoded to be 100 and does not change
        private const val CAMERA_PERMISSION_CODE = 100
    }

    // pretty much same as a void start func
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // sets the UI as vars using the set ID's
        previewView = findViewById(R.id.previewView)
        cameraButton = findViewById(R.id.cameraButton)
        switchCamButton = findViewById(R.id.switchCamButton)

        cameraExecutor = Executors.newSingleThreadExecutor()

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
    }

    private fun hasCameraPermission(): Boolean {
        // checks if user already gave perms
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        // requests for camera perms
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
    }

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

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCamera()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCamera() {
        val cameraProvider = cameraProvider ?: return

        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }

        imageCapture = ImageCapture.Builder().build()

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
        } catch(e: Exception) {
            Toast.makeText(this, "Camera failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(externalMediaDirs.firstOrNull(), "flashback_${System.currentTimeMillis()}.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
            override fun onError(exception: ImageCaptureException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Capture failed ${exception.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Saved: ${photoFile.absolutePath}", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun switchCam() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
            CameraSelector.DEFAULT_FRONT_CAMERA
        else
            CameraSelector.DEFAULT_BACK_CAMERA

        bindCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }


}