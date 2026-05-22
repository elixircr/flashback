package com.hwaranga.flashback

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// broadcast receiver class; runs when the broadcast is received
class MoveScreenshotReceiver : BroadcastReceiver() {

    // function runs when broadcast received
    // copys screenshotted photo over to apps gallery
    override fun onReceive(context: Context, intent: Intent) {
        val filePath = intent.getStringExtra("file_path") ?: return
        val sourceFile = File(filePath)
        if (!sourceFile.exists()) return

        val dateString = SimpleDateFormat("ddMMyy_HHmmss", Locale.getDefault()).format(Date())
        val destFile = File(context.filesDir, "flashback_${dateString}_1d.jpg")

        sourceFile.copyTo(destFile, overwrite = true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(2)
    }
}