package com.cheese.wallpaperscheduler.scheduler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import com.cheese.wallpaperscheduler.R
import com.cheese.wallpaperscheduler.data.WallpaperSlot

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "wallpaper_changes"
        private const val NOTIFICATION_ID = 42
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Wallpaper changes",
                NotificationManager.IMPORTANCE_LOW,
            )
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    fun showWallpaperChangedNotification(slot: WallpaperSlot) {
        // Required as of API 33; if the user never granted it we simply skip the notification
        // rather than crash — the wallpaper change itself already succeeded regardless.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val (emoji, label) = when (slot) {
            WallpaperSlot.LIGHT -> "☀️" to "Light wallpaper is now active"
            WallpaperSlot.DARK -> "🌙" to "Dark wallpaper is now active"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("$emoji $label")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
