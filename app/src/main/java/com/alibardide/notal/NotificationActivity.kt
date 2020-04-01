package com.alibardide.notal

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat

class NotificationActivity : AppCompatActivity() {

    var id: Int? = null
    var text: String? = null
    var pin: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = intent.extras ?: return
        id = bundle.getInt("id")
        text = bundle.getString("text")
        pin = bundle.getBoolean("pin")

        showNotification()
    }
    private fun showNotification() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_name))
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
                with (NotificationManagerCompat.from(this)) { cancel(id!!) }
            }.setNeutralButton(R.string.no) { _: DialogInterface, _: Int ->
                showNotification()
            }.create()
        alertDialog.show()
    }
    private fun editNotification() {
        startActivity(Intent(this@NotificationActivity, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("id", id)
            putExtra("text", text)
            putExtra("pin", pin)
            putExtra("edit", true) })
        finish()
    }
}
