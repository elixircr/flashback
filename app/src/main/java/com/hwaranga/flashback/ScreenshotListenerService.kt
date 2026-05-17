package com.hwaranga.flashback

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.File

class ScreenshotListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val title = sbn.notification.extras.getString("android.title") ?: return
        val text = sbn.notification.extras.getString("android.text") ?: return

        if (title.contains("Screenshot saved", ignoreCase = true) &&
            text.contains("Tap here to see your screenshot.", ignoreCase = true)) {

            Handler(Looper.getMainLooper()).postDelayed({
                val filePath = getLatestScreenshot()
                if (filePath != null) sendMoveNotification(filePath)
            }, 1000)
        }
    }

    private fun getLatestScreenshot(): String? {
        val screenshotDir = File("/storage/emulated/0/DCIM/Screenshots")
        return screenshotDir.listFiles()
            ?.filter { it.extension == "jpg" || it.extension == "png" }
            ?.maxByOrNull { it.lastModified() }
            ?.absolutePath
    }

    private fun sendMoveNotification(filePath: String) {
        val moveIntent = Intent(this, MoveScreenshotReceiver::class.java)
        moveIntent.putExtra("file_path", filePath)
        val pendingIntent = PendingIntent.getBroadcast(
            this, filePath.hashCode(), moveIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "flashback_channel")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Screenshot detected")
            .setContentText("Tap to move to Flashback")
            .addAction(R.mipmap.ic_launcher, "Move to Flashback", pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(2, notification)
    }
}