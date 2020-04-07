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
        val KEY_ID = "id"
        val KEY_TEXT = "text"
        val KEY_PIN = "pin"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bundle = intent.extras
        if (bundle != null) {
            val id = bundle.getInt(KEY_ID)
            val text = bundle.getString(KEY_TEXT)
            val pin = bundle.getBoolean(KEY_PIN)
            if (text != null) {
                editMode(id, text, pin)
                return
            }
        }

        mainCardCreate.setOnClickListener {
            if (getNote().toString().trim() == "")
                Toast.makeText(applicationContext, getString(R.string.empty_note_error), Toast.LENGTH_SHORT).show()
            else {
                makeNotification()
                setNote("")
            }
        }
    }
    private fun makeNotification(currentId: Int? = null) {
        val preferences = getSharedPreferences("s", Context.MODE_PRIVATE)
        val id = currentId ?: preferences.getInt(KEY_ID, 0)
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(KEY_ID, id)
            putExtra(KEY_TEXT, getNote().toString())
            putExtra(KEY_PIN, isPinned())
        }
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = newNotification(pendingIntent)

        createNotificationChannel()
        with (NotificationManagerCompat.from(this)) {
            notify(id, notification)
        }
        if (currentId == null)
            preferences.edit().putInt("id", id + 1).apply()
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("0", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun editMode(id: Int, text: String, pin: Boolean) {
        setPinned(pin)
        setNote(text)
        mainTextViewCreateCardText.setText(R.string.btn_main_edit)
        mainCardCreate.setOnClickListener {
            if (getNote().toString().trim() == "")
                Toast.makeText(applicationContext, getString(R.string.empty_note_error), Toast.LENGTH_SHORT).show()
            else if (getNote().toString() == text && isPinned() == pin)
                Toast.makeText(this, getString(R.string.same_note_error), Toast.LENGTH_SHORT).show()
            else
                makeNotification(id)
        }
    }
    private fun getNote(): Editable = mainEditTextNote.text
    private fun setNote(note: String) { mainEditTextNote.setText(note) }
    private fun isPinned(): Boolean = mainCheckBoxPin.isChecked
    private fun setPinned(pin: Boolean) { mainCheckBoxPin.isChecked = pin }
    private fun newNotification(pendingIntent: PendingIntent): Notification {
        return NotificationCompat.Builder(applicationContext, "0")
            .setContentIntent(pendingIntent)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getNote().toString())
            .setOngoing(isPinned())
            .setSmallIcon(R.mipmap.ic_ticker)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setTicker(getString(R.string.app_name))
            .build()
    }
}
