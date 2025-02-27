package com.alibardide.notal.utils

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationManagerCompat
import com.alibardide.notal.Constants
import com.alibardide.notal.data.NoteDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var noteDao: NoteDao

    override fun onReceive(context: Context?, intent: Intent?) {
        // Check if created on boot completed
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        context?.let {
            CoroutineScope(Dispatchers.IO).launch {
                if (
                    Constants.isAtLeastTiramisu() &&
                    context.checkSelfPermission(
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    noteDao.getAllNotes().forEach { note ->
                        val notification = NotificationUtil(context)
                            .provideNotification(note.id, note.text)
                        NotificationManagerCompat.from(context).notify(note.id, notification)
                    }
                }
            }
        }
    }
}