package com.alibardide.notal

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat

class NotificationActivity : AppCompatActivity() {

    private lateinit var preferences: SharedPreferences
    private lateinit var note: Note

    override fun onCreate(savedInstanceState: Bundle?) {
        // Change theme to DarkMode
        preferences = getSharedPreferences(MainActivity.KEY_PREFERENCES, Context.MODE_PRIVATE)
        if (preferences.getBoolean(MainActivity.KEY_DARK, false)) setTheme(R.style.DarkTheme)
        super.onCreate(savedInstanceState)
        // Check if intent has data
        if (intent.hasExtra(MainActivity.KEY_EDIT)) {
            note = intent.getSerializableExtra(MainActivity.KEY_EDIT) as Note
        } else finish()
        // Show the dialog
        notificationDialog()
    }
    private fun notificationDialog() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle(R.string.app_name)
            .setMessage(note.text)
            .setCancelable(false)
            .setPositiveButton(R.string.ok) { _: DialogInterface, _: Int -> finish() }
            .setNegativeButton(R.string.edit) { _: DialogInterface, _: Int -> editNotification() }
            .setNeutralButton(R.string.delete, null)
        val dialog = alertDialog.create()
        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            var delete = false
            button.setTextColor(Color.parseColor("#f44336"))
            button.setOnClickListener {
                if (delete) {
                    NotificationManagerCompat.from(this).cancel(note.id)
                    finish()
                } else {
                    delete = true
                    object: CountDownTimer(3000, 1000) {
                        override fun onFinish() { delete = false }
                        override fun onTick(p0: Long) {} }.start()
                    Toast.makeText(
                        this, "Tap again to delete", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }
    // Return to MainActivity.kt and edit current notification
    private fun editNotification() {
        // send data using intent
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(MainActivity.KEY_EDIT, note)
        }
        startActivity(intent)
    }
}
