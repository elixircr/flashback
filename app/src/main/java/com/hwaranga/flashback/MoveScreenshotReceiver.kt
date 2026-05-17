package com.hwaranga.flashback

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MoveScreenshotReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val filePath = intent.getStringExtra("file_path") ?: return
        val sourceFile = File(filePath)
        if (!sourceFile.exists()) return

        val sdf = SimpleDateFormat("ddMMyy_HHmmss", Locale.getDefault())
        val dateString = sdf.format(Date())
        val destFile = File(context.filesDir, "flashback_${dateString}_1d.jpg")

        sourceFile.copyTo(destFile, overwrite = true)
        sourceFile.delete()
        Log.d("Flashback", "Screenshot moved to: ${destFile.absolutePath}")
    }
}