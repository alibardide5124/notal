package com.alibardide.notal.utils

import android.content.*
import com.alibardide.notal.database.AppDatabase

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(p0: Context?, p1: Intent?) {
        // Check if created on boot completed
        if (p1?.action == Intent.ACTION_BOOT_COMPLETED) {
            p0?.let { context ->
                AppDatabase(context).findNotes().forEach { note ->
                    // Create notification for each note
                    NotificationUtil(context).createNotification(note.id, note.text)
                }
            }
        }
    }

}