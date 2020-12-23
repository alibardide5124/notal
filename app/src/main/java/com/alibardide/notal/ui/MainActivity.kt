package com.alibardide.notal.ui

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alibardide.notal.model.Note
import com.alibardide.notal.R
import com.alibardide.notal.utils.NotificationUtil
import com.alibardide.notal.database.AppDatabase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val KEY_EDIT = "edit"
        const val KEY_DARK = "isDark"
        const val KEY_PREFERENCES = "preferences"
        const val KEY_CHECKPOINT = "checkpoint"
    }

    private lateinit var preferences: SharedPreferences
    private lateinit var database: AppDatabase
    private var isDarkMode = false
    private var note: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Change theme to DarkMode
        preferences = getSharedPreferences(KEY_PREFERENCES, Context.MODE_PRIVATE)
        isDarkMode = preferences.getBoolean(KEY_DARK, false)
        if (isDarkMode) setTheme(R.style.DarkTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Init database
        database = AppDatabase(this)
        // Check if is editing note
        if (intent.hasExtra(KEY_EDIT)) {
            note = intent.getSerializableExtra(KEY_EDIT) as Note
            setNote(note?.text!!)
            setButtonText(getString(R.string.btn_main_edit))
        }
        // check if has saved data when change theme
        if (intent.hasExtra(KEY_CHECKPOINT)) setNote(intent.extras?.getString(
            KEY_CHECKPOINT
        )!!)
        // Change app theme and reset activity
        imageViewTheme.setOnClickListener { changeTheme() }
        // About me dialog
        imageViewAbout.setOnClickListener { aboutDialog().show() }
        // Add a new note or save edited note
        btnCreate.setOnClickListener {
            when {
                // Check if user input empty text
                getNote().toString().trim() == "" -> toast(getString(R.string.empty_note_error))
                // Check if user edit note and haven't change anything
                note != null && getNote().toString() == note?.text -> toast(getString(
                    R.string.same_note_error
                ))
                // Create notification if everything is OK
                else -> apply()
            }
        }
    }
    // Change app theme
    private fun changeTheme() {
        preferences.edit().putBoolean(KEY_DARK, !isDarkMode).apply()
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(KEY_CHECKPOINT, getNote().toString())
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
    // Apply notification change or create a new one
    private fun apply() {
        // Create notification or update
        val id = NotificationUtil(this).createNotification(note?.id, getNote().toString())
        if (note != null) {
            // Apply changes and quit app
            toast(getString(R.string.changes_applied))
            database.update(id.toString(), Note(id, getNote().toString()))
            finish()
        } else {
            // Apply changes and delete text
            toast(getString(R.string.notification_created))
            database.save(Note(id ,getNote().toString()))
            setNote("")
        }
    }

}