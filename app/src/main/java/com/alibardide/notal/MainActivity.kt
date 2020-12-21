package com.alibardide.notal

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val KEY_ID = "id"
        const val KEY_DARK = "isDark"
        const val KEY_PREFERENCES = "preferences"
        const val KEY_CHECKPOINT = "checkpoint"
        const val KEY_EDIT = "edit"
    }

    private lateinit var preferences: SharedPreferences
    private var isDarkMode = false
    private var note: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Change theme to DarkMode
        preferences = getSharedPreferences(KEY_PREFERENCES, Context.MODE_PRIVATE)
        isDarkMode = preferences.getBoolean(KEY_DARK, false)
        if (isDarkMode) setTheme(R.style.DarkTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Check if is editing note
        if (intent.hasExtra(KEY_EDIT)) {
            note = intent.getSerializableExtra(KEY_EDIT) as Note
            setNote(note?.text!!)
            setButtonText(getString(R.string.btn_main_edit))
        }
        // check if has saved data when change theme
        if (intent.hasExtra(KEY_CHECKPOINT)) setNote(intent.extras?.getString(KEY_CHECKPOINT)!!)
        // Change app theme and reset activity
        imageViewTheme.setOnClickListener { changeTheme() }
        // About me dialog
        imageViewAbout.setOnClickListener { aboutDialog().show() }
        // Add a new note or save edited note
        btnCreate.setOnClickListener {
            when {
                getNote().toString().trim() == "" -> toast(getString(R.string.empty_note_error))
                note != null && getNote().toString() == note?.text -> toast(getString(R.string.same_note_error))
                else -> apply()
            }
        }
    }
    // Make a notification to show in notification bar
    private fun makeNotification(currentId: Int? = null) {
        val id = currentId ?: preferences.getInt(KEY_ID, 0)
        // Start NotificationActivity.kt when touch notification
        val intent = Intent(this, NotificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(KEY_EDIT, Note(id, getNote().toString()))
        }
        val pendingIntent = PendingIntent.getActivity(applicationContext, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        // Make a new notification
        val notification = getNotification(pendingIntent)
        // Create notification to use
        createNotificationChannel()
        // notify notification
        NotificationManagerCompat.from(this).notify(id, notification)
        if (currentId == null) preferences.edit().putInt(KEY_ID, id + 1).apply()
    }
    // add a function to create a notification
    private fun getNotification(pendingIntent: PendingIntent): Notification {
        return NotificationCompat.Builder(applicationContext, "0")
            .setContentIntent(pendingIntent)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getNote().toString())
            .setOngoing(true)
            .setSmallIcon(R.mipmap.ic_ticker)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setTicker(getString(R.string.app_name))
            .build()
    }
    // create a notification channel
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("0", name, importance).apply {
                description = descriptionText
            }
            NotificationManagerCompat.from(this).createNotificationChannel(channel)
        }
    }
    // Change app theme
    private fun changeTheme() {
        preferences.edit().putBoolean(KEY_DARK, !isDarkMode).apply()
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(KEY_CHECKPOINT, getNote().toString())
        }
        startActivity(intent)
        finish()
    }
    // About mee dialog
    private fun aboutDialog() : AlertDialog {
        return AlertDialog.Builder(this)
            .setTitle(R.string.about_me)
            .setMessage(R.string.about_me_message)
            .setPositiveButton(R.string.ok, null)
            .create()
    }
    // Getters and Setters
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    private fun getNote(): Editable = editTextNote.text
    private fun setNote(note: String) { editTextNote.setText(note) }
    private fun setButtonText(text: String) { btnCreate.text = text }
    private fun apply() {
        makeNotification(note?.id)
        if (note != null) {
            toast(getString(R.string.changes_applied))
            finish()
        } else {
            toast(getString(R.string.notification_created))
            setNote("")
        }
    }

}