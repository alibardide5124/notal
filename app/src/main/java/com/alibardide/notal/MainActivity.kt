package com.alibardide.notal

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
        const val KEY_PIN = "pin"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (getPreferences(Context.MODE_PRIVATE).getBoolean("dark", false))
            setTheme(R.style.DarkTheme)
        setContentView(R.layout.activity_main)

        val bundle = intent.extras
        if (bundle != null) {
            if (editNote(bundle))
                return
        }

        cardViewCreate.setOnClickListener {
            if (getNote().toString().trim() == "")
                toast(getString(R.string.empty_note_error))
            else {
                makeNotification()
                setNote("")
            }
        }
    }
    private fun makeNotification(currentId: Int? = null) {
        val preferences = getSharedPreferences("s", Context.MODE_PRIVATE)
        val id = currentId ?: preferences.getInt(KEY_ID, 0)
        val intent = Intent(this, NotificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(KEY_ID, id)
            putExtra(KEY_TEXT, getNote().toString())
            putExtra(KEY_PIN, isPinned())
        }
        val pendingIntent = PendingIntent.getActivity(applicationContext, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = newNotification(pendingIntent)

        createNotificationChannel()
        NotificationManagerCompat.from(this).notify(id, notification)
        if (currentId == null)
            preferences.edit().putInt("id", id + 1).apply()
    }
    private fun newNotification(pendingIntent: PendingIntent): Notification {
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
    private fun editNote(bundle: Bundle): Boolean {
        val id = bundle.getInt(KEY_ID)
        val text = bundle.getString(KEY_TEXT)
        val pin = bundle.getBoolean(KEY_PIN)
        if (text == null)
            return false
        setPinned(pin)
        setNote(text)
        mainTextViewCreateCardText.setText(R.string.btn_main_edit)
        cardViewCreate.setOnClickListener {
            if (getNote().toString().trim() == "")
                toast(getString(R.string.empty_note_error))
            else if (getNote().toString() == text && isPinned() == pin)
                toast(getString(R.string.same_note_error))
            else {
                makeNotification(id)
                toast(getString(R.string.changes_applied))
                finish()
            }
        }
        return true
    }
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    private fun getNote(): Editable = editTextNote.text
    private fun setNote(note: String) { editTextNote.setText(note) }
    private fun isPinned(): Boolean = checkBoxPin.isChecked
    private fun setPinned(pin: Boolean) { checkBoxPin.isChecked = pin }

}
