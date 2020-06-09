package com.alibardide.notal

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat

class NotificationActivity : AppCompatActivity() {

    private var preferences: SharedPreferences? = null
    private var id: Int = 0
    private var text: String = ""
    private var pin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Change theme to DarkMode
        preferences = getSharedPreferences(MainActivity.KEY_PREFERENCES, Context.MODE_PRIVATE)
        if (preferences!!.getBoolean(MainActivity.KEY_DARK, false)) setTheme(R.style.DarkTheme)

        super.onCreate(savedInstanceState)

        // Check if intent has data
        if (intent.hasExtra(MainActivity.KEY_EDIT_DATA)) {
            val bundle = intent?.extras?.getBundle(MainActivity.KEY_EDIT_DATA)!!
            id = bundle.getInt(MainActivity.KEY_ID)
            text = bundle.getString(MainActivity.KEY_TEXT)!!
            pin = bundle.getBoolean(MainActivity.KEY_PIN)
        } else finish()

        // Show the dialog
        notificationDialog()
    }
    private fun notificationDialog() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle(R.string.app_name)
            .setMessage(text)
            .setCancelable(false)
            .setPositiveButton(R.string.ok) { _: DialogInterface, _: Int ->
                finish()
            }.setNegativeButton(R.string.edit) { _: DialogInterface, _: Int ->
                editNotification()
            }.setNeutralButton(R.string.delete) { _: DialogInterface, _: Int ->
                deleteNotification()
            }.create()
        alertDialog.show()
    }
    // Ask for delete a note
    private fun deleteNotification() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle(R.string.delete)
            .setMessage(R.string.delete_message)
            .setCancelable(false)
            .setPositiveButton(R.string.yes) { _: DialogInterface, _: Int ->
                NotificationManagerCompat.from(this).cancel(id)
                finish()
            }.setNeutralButton(R.string.no) { _: DialogInterface, _: Int ->
                notificationDialog()
            }.create()
        alertDialog.show()
    }
    // Return to MainActivity.kt and edit current notification
    private fun editNotification() {
        val data = Bundle()
        data.putInt(MainActivity.KEY_ID, id)
        data.putString(MainActivity.KEY_TEXT, text)
        data.putBoolean(MainActivity.KEY_PIN, pin)
        // send data using intent
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(MainActivity.KEY_EDIT_DATA, data)
        }
        startActivity(intent)
    }
}
