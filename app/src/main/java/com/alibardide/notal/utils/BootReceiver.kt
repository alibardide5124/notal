package com.alibardide.notal.utils

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.alibardide.notal.data.Note
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
        Log.i("Boot", "Boot received")
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            context?.let {
                val notes = mutableListOf<Note>()
                CoroutineScope(Dispatchers.IO).launch {
                    notes.addAll(noteDao.getAllNotes())
                }.invokeOnCompletion {
                    Log.i("Boot", "Checking permission")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        if (context.checkSelfPermission(
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_DENIED
                        )
                            return@invokeOnCompletion

                    Log.i("Boot", "Has permission")

                    notes.forEach { note ->
                        Log.i("Boot", "Got ${note.id}: ${note.text}")
                        val notification = NotificationUtil(context)
                            .provideNotification(note.id, note.text)
                        NotificationManagerCompat.from(context).notify(note.id, notification)
                    }
                }
            }
        }
    }
}