package com.alibardide.notal.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.alibardide.notal.R
import com.alibardide.notal.model.Note
import com.alibardide.notal.ui.MainActivity
import com.alibardide.notal.ui.NotificationActivity

class NotificationUtil(private val context: Context) {

    companion object {
        const val KEY_ID = "id"
    }
    // Make a notification to show in notification bar
    fun createNotification(currentId: Int? = null, text: String) : Int {
        val preferences =
            context.getSharedPreferences(MainActivity.KEY_PREFERENCES, Context.MODE_PRIVATE)
        val id = currentId ?: preferences.getInt(KEY_ID, 0)
        // Start NotificationActivity.kt when touch notification
        val intent = Intent(context, NotificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(MainActivity.KEY_EDIT, Note(id, text))
        }
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        // Create a new notification
        val notification = getNotification(pendingIntent, text)
        // Create notification to use
        createNotificationChannel()
        // notify notification
        NotificationManagerCompat.from(context).notify(id, notification)
        // Update id
        if (currentId == null) preferences.edit().putInt(KEY_ID, id + 1).apply()
        // Return id for mote uses
        return id
    }
    // add a function to create a notification
    private fun getNotification(pendingIntent: PendingIntent, text: String): Notification {
        return NotificationCompat.Builder(context, "0")
            .setContentIntent(pendingIntent)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(text)
            .setOngoing(true)
            .setSmallIcon(R.mipmap.ic_ticker)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setTicker(context.getString(R.string.app_name))
            .build()
    }
    // create a notification channel
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("0", name, importance).apply {
                description = descriptionText
            }
            NotificationManagerCompat.from(context).createNotificationChannel(channel)
        }
    }
}