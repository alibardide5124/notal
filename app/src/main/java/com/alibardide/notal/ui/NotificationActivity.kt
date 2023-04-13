package com.alibardide.notal.ui

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.alibardide.notal.R
import com.alibardide.notal.data.Note
import com.alibardide.notal.data.NoteDao
import com.alibardide.notal.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationActivity : AppCompatActivity() {

    @Inject
    lateinit var noteDao: NoteDao
    private lateinit var note: Note

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (hasNoteFromIntent())
            displayNotificationDialog()
        else
            finish()
    }

    private fun hasNoteFromIntent(): Boolean {
        return if (intent.hasExtra(Constants.KEY_EDIT)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                note = intent.getSerializableExtra(Constants.KEY_EDIT, Note::class.java)!!
            else
                @Suppress("DEPRECATION")
                note = intent.getSerializableExtra(Constants.KEY_EDIT) as Note
            true
        } else
            false
    }

    private fun displayNotificationDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.app_name)
            .setMessage(note.text.trim())
            .setCancelable(false)
            .setPositiveButton(R.string.ok) { _: DialogInterface, _: Int -> finish() }
            .setNegativeButton(R.string.edit) { _: DialogInterface, _: Int -> editNotification() }
            .setNeutralButton(R.string.delete, null)
            .create()
        // Apply changes in delete button
        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            var delete = false
            button.setTextColor(Color.parseColor("#f44336"))
            button.setOnClickListener {
                if (delete) {
                    NotificationManagerCompat.from(this).cancel(note.id)
                    CoroutineScope(Dispatchers.IO).launch {
                        noteDao.deleteNote(note)
                    }.invokeOnCompletion {
                        finish()
                    }
                } else {
                    // Show 'Tap again to delete' message
                    delete = true
                    object: CountDownTimer(3000, 1000) {
                        override fun onFinish() { delete = false }
                        override fun onTick(p0: Long) {} }.start()
                    Toast.makeText(this, "Tap again to delete", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        dialog.show()
    }
    // Return to MainActivity.kt and edit current notification
    private fun editNotification() {
        // send note data using intent
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(Constants.KEY_EDIT, note)
        }
        startActivity(intent)
    }
}
