package com.alibardide.notal

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver : BroadcastReceiver() {

    private var id: Int? = null
    private var text: String? = null
    private var pin: Boolean? = null

    private lateinit var context: Context

    override fun onReceive(context: Context?, intent: Intent?) {
        val bundle = intent?.extras ?: return
        id = bundle.getInt(MainActivity.KEY_ID)
        text = bundle.getString(MainActivity.KEY_TEXT)
        pin = bundle.getBoolean(MainActivity.KEY_PIN)
        this.context = context!!

        notificationDialog()
    }
    private fun notificationDialog() {
        val alertDialog = AlertDialog.Builder(context)
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
        val alertDialog = AlertDialog.Builder(context)
            .setTitle(R.string.delete)
            .setMessage(R.string.delete_message)
            .setPositiveButton(R.string.yes) { _: DialogInterface, _: Int ->
                with (NotificationManagerCompat.from(context)) { cancel(id!!) }
            }.setNeutralButton(R.string.no) { _: DialogInterface, _: Int ->
                notificationDialog()
            }.create()
        alertDialog.show()
    }
    private fun editNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(MainActivity.KEY_ID, id)
            putExtra(MainActivity.KEY_TEXT, text)
            putExtra(MainActivity.KEY_PIN, pin)
        }
        (context as Activity).startActivity(intent)
    }
}
