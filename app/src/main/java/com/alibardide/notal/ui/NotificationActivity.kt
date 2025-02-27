package com.alibardide.notal.ui

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationActivity : AppCompatActivity() {

    @Inject
    lateinit var noteDao: NoteDao
    private lateinit var note: Note

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasNoteFromIntent()) finish()

        getNoteFromIntent()
        displayNotificationDialog()
    }

    private fun hasNoteFromIntent(): Boolean =
        intent.hasExtra(Constants.KEY_EDIT)

    private fun getNoteFromIntent() {
        note = when {
            Constants.isAtLeastTiramisu() ->
                intent.getSerializableExtra(
                    Constants.KEY_EDIT,
                    Note::class.java
                )!!

            else -> {
                @Suppress("DEPRECATION")
                intent.getSerializableExtra(Constants.KEY_EDIT) as Note
            }
        }
    }

    private fun displayNotificationDialog() {
        val dialog =
            AlertDialog.Builder(this).setTitle(R.string.app_name).setMessage(note.text.trim())
                .setCancelable(false)
                .setPositiveButton(R.string.ok) { _: DialogInterface, _: Int ->
                    finish()
                }
                .setNegativeButton(R.string.edit) { _: DialogInterface, _: Int ->
                    startEditInMainActivity()
                }
                .setNeutralButton(R.string.delete, null).create()
        // Apply changes in delete button
        dialog.setOnShowListener {
            val deleteButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            var canDelete = false

            deleteButton.setTextColor(Color.parseColor("#f44336"))
            deleteButton.setOnClickListener {
                if (canDelete) {
                    CoroutineScope(Dispatchers.IO).launch {
                        noteDao.deleteNote(note)
                        NotificationManagerCompat.from(coroutineContext as Context).cancel(note.id)
                    }.invokeOnCompletion {
                        finish()
                    }
                } else {
                    // Show 'Tap again to delete' message
                    CoroutineScope(Dispatchers.IO).launch {
                        canDelete = true
                        delay(3000)
                        canDelete = false
                    }
                    Toast.makeText(this, "Tap again to delete", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    // Return to MainActivity.kt and edit current notification
    private fun startEditInMainActivity() {
        // send note data using intent
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(Constants.KEY_EDIT, note)
        }
        startActivity(intent)
    }
}
