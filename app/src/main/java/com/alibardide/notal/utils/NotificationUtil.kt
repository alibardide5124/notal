package com.alibardide.notal.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.alibardide.notal.Constants
import com.alibardide.notal.R
import com.alibardide.notal.data.Note
import com.alibardide.notal.ui.NotificationActivity

class NotificationUtil(private val context: Context) {

    fun provideNotification(id: Int, text: String): Notification {
        // Start NotificationActivity.kt when touch notification
        val intent = Intent(context, NotificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(Constants.KEY_EDIT, Note(id, text))
        }

        val pendingIntent = when {
            Constants.isAtLeastMarshmallow() ->
                PendingIntent.getActivity(
                    context,
                    id,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

            else ->
                PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        return NotificationCompat.Builder(context, context.getString(R.string.channel_id))
            .setContentIntent(pendingIntent)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(text)
            .setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setTicker(context.getString(R.string.app_name))
            .build()
    }

    fun createNotificationChannel() {
        if (Constants.isAtLeastOreo()) {
            val channel = NotificationChannel(
                context.getString(R.string.channel_id),
                context.getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = context.getString(R.string.channel_description) }
            NotificationManagerCompat.from(context).createNotificationChannel(channel)
        }
    }
}