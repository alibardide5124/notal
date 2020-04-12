package com.alibardide.notal

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat

class NotificationActivity : AppCompatActivity() {

    private var id: Int? = null
    private var text: String? = null
    private var pin: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = intent?.extras ?: return
        id = bundle.getInt(MainActivity.KEY_ID)
        text = bundle.getString(MainActivity.KEY_TEXT)
        pin = bundle.getBoolean(MainActivity.KEY_PIN)

        notificationDialog()
    }
    private fun notificationDialog() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle(R.string.app_name)
            .setMessage(text)
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.edit) { _: DialogInterface, _: Int ->
                editNotification()
            }.setNeutralButton(R.string.delete) { _: DialogInterface, _: Int ->
                deleteNotification()
            }.create()
        alertDialog.show()
    }
    private fun deleteNotification() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle(R.string.delete)
            .setMessage(R.string.delete_message)
            .setPositiveButton(R.string.yes) { _: DialogInterface, _: Int ->
                NotificationManagerCompat.from(this).cancel(id!!)
            }.setNeutralButton(R.string.no) { _: DialogInterface, _: Int ->
                notificationDialog()
            }.create()
        alertDialog.show()
    }
    private fun editNotification() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(MainActivity.KEY_ID, id)
            putExtra(MainActivity.KEY_TEXT, text)
            putExtra(MainActivity.KEY_PIN, pin)
        }
        startActivity(intent)
    }
}
