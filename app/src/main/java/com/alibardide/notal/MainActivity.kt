package com.alibardide.notal

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bundle = intent.extras
        if (bundle != null) {
            val text = bundle.getString("text")
            val id = bundle.getInt("id")
            val edit = bundle.getBoolean("edit")
            val pin = bundle.getBoolean("pin")
            edt_note.setText(text)
            if (edit) {
                enableEditMode(id, text!!, pin)
                return
            }
        }

        btn_create_text.setText(R.string.btn_main)
        btn_create.setOnClickListener {
            if (edt_note.text.toString().trim() == "")
                Toast.makeText(applicationContext, getString(R.string.empty_note_error), Toast.LENGTH_SHORT).show()
            else {
                createNotification(null)
                edt_note.setText("")
            }
        }
    }
    private fun createNotification(currentId: Int?) {
        val preferences = getSharedPreferences("s", Context.MODE_PRIVATE)
        val id = currentId ?: preferences.getInt("id", 0)
        val intent = Intent(this@MainActivity, NotificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("id", id)
            putExtra("text", edt_note.text.toString())
            putExtra("pin", ckb_pin.isChecked)
        }
        val pendingIntent = PendingIntent.getActivity(applicationContext, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = NotificationCompat.Builder(applicationContext, "0")
            .setContentIntent(pendingIntent)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(edt_note.text.toString())
            .setOngoing(ckb_pin.isChecked)
            .setSmallIcon(R.mipmap.ic_ticker)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setTicker(getString(R.string.app_name))

        createNotificationChannel()
        with (NotificationManagerCompat.from(this)) {
            notify(id, notification.build())
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
    private fun enableEditMode(id: Int, text: String, pin: Boolean) {
        btn_create_text.setText(R.string.btn_main_edit)
        ckb_pin.isChecked = pin
        btn_create.setOnClickListener {
            when {
                edt_note.text.toString().trim() == "" -> Toast.makeText(applicationContext, getString(R.string.empty_note_error), Toast.LENGTH_SHORT).show()
                edt_note.text.toString() == text -> Toast.makeText(this, getString(R.string.same_note_error), Toast.LENGTH_SHORT).show()
                else -> createNotification(id)
            }
        }
    }
}
