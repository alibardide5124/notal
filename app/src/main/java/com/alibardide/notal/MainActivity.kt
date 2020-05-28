package com.alibardide.notal

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
        const val KEY_TEXT = "text"
        const val KEY_PIN = "isPin"
        const val KEY_DARK = "isDark"
        const val KEY_PREFERENCES = "preferences"
        const val KEY_SAVE_DATA = "saveData"
        const val KEY_EDIT_DATA = "editData"
        const val KEY_NOTE_SAVE = "noteSave"
        const val KEY_PIN_SAVE  = "pinSave"
    }

    private lateinit var preferences: SharedPreferences
    private var isEditing = false
    private var id: Int? = null
    private var text: String? = null
    private var isPin: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Change theme to DarkMode
        preferences = getSharedPreferences(KEY_PREFERENCES, Context.MODE_PRIVATE)
        val isDark = preferences.getBoolean(KEY_DARK, false)
        if (isDark) setTheme(R.style.DarkTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if is editing note
        if (intent.hasExtra(KEY_EDIT_DATA)) {
            val bundle = intent.extras?.getBundle(KEY_EDIT_DATA)
            id = bundle?.getInt(KEY_ID)
            text = bundle?.getString(KEY_TEXT)
            isPin = bundle?.getBoolean(KEY_PIN)
            isEditing = true

            setNote(text!!)
            setPinned(isPin!!)
            setButtonText(getString(R.string.btn_main_edit))
        }
        // check if has saved data when change theme
        if (intent.hasExtra(KEY_SAVE_DATA)) {
            val extras = intent.extras?.getBundle(KEY_SAVE_DATA)!!
            setNote(extras.getString(KEY_NOTE_SAVE)!!)
            setPinned(extras.getBoolean(KEY_PIN_SAVE))
        }

        // Change theme and reset activity
        imageViewTheme.setOnClickListener {
            preferences.edit().putBoolean(KEY_DARK, !isDark).apply()
            val data = Bundle()
            data.putString(KEY_NOTE_SAVE, getNote().toString())
            data.putBoolean(KEY_PIN_SAVE, isPinned())
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra(KEY_SAVE_DATA, data)
            }
            startActivity(intent)
            finish()
        }
        // Add a new note or save edited note
        cardViewCreate.setOnClickListener {
            if (isEditing) {
                if (getNote().toString().trim() == "")
                    toast(getString(R.string.empty_note_error))
                else if (getNote().toString() == text && isPinned() == isPin)
                    toast(getString(R.string.same_note_error))
                else {
                    makeNotification(id)
                    toast(getString(R.string.changes_applied))
                    finish()
                }
            } else {
                if (getNote().toString().trim() == "")
                    toast(getString(R.string.empty_note_error))
                else {
                    makeNotification()
                    setNote("")
                }
            }
        }

    }

    // Make a notification to show in notification bar
    private fun makeNotification(currentId: Int? = null) {
        val id = currentId ?: preferences.getInt(KEY_ID, 0)
        // Use bundle to store note data in intent
        val bundle = Bundle()
        bundle.putInt(KEY_ID, id)
        bundle.putString(KEY_TEXT, getNote().toString())
        bundle.putBoolean(KEY_PIN, isPinned())
        // Start NotificationActivity.kt when touch notification
        val intent = Intent(this, NotificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(KEY_EDIT_DATA, bundle)
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
            .setOngoing(isPinned())
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

    // Getters and Setters
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    private fun getNote(): Editable = editTextNote.text
    private fun setNote(note: String) { editTextNote.setText(note) }
    private fun isPinned(): Boolean = checkBoxPin.isChecked
    private fun setPinned(pin: Boolean) { checkBoxPin.isChecked = pin }
    private fun setButtonText(text: String) { mainTextViewCreateCardText.text = text }

}
