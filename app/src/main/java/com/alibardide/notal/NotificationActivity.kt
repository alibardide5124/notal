package com.alibardide.notal

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat

class NotificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = intent.extras
        val id = bundle?.getInt("id")
        val text = bundle?.getString("text")

        showNotification(id!!, text!!)
    }
    private fun showNotification(id: Int, text: String) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_name))
            .setMessage(text)
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.edit) { _: DialogInterface, _: Int ->
                editNotification(id, text)
            }
            .setNeutralButton(R.string.delete) { _: DialogInterface, _: Int ->
                deleteNotification(id, text)
            }
            .create()
        alertDialog.show()
    }
    private fun deleteNotification(id: Int, text: String) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle(R.string.delete)
            .setMessage(R.string.delete_message)
            .setPositiveButton(R.string.yes) { _: DialogInterface, _: Int ->
                with (NotificationManagerCompat.from(this)) { cancel(id) }
            }
            .setNeutralButton(R.string.no) { _: DialogInterface, _: Int ->
                showNotification(id, text)
            }
            .create()
        alertDialog.show()
    }
    private fun editNotification(id: Int, text: String) {
        startActivity(Intent(this@NotificationActivity, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("id", id)
            putExtra("text", text)
            putExtra("edit", true)
        })
        finish()
    }
}
