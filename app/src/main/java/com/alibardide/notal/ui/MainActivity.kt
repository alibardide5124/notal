package com.alibardide.notal.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.alibardide.notal.BuildConfig
import com.alibardide.notal.R
import com.alibardide.notal.data.Note
import com.alibardide.notal.data.NoteDao
import com.alibardide.notal.databinding.ActivityMainBinding
import com.alibardide.notal.Constants
import com.alibardide.notal.utils.NotificationUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity: AppCompatActivity() {

    @Inject
    lateinit var prefs: SharedPreferences
    @Inject
    lateinit var noteDao: NoteDao
    private lateinit var binding: ActivityMainBinding
    private var note: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val context = this@MainActivity

        // Check if note sent from intent
        updateNoteFromIntent()
        NotificationUtil(this).createNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && hasNotificationPermission().not())
            requestNotificationPermission()

        binding.imageViewAbout.setOnClickListener { displayAboutDialog().show() }
        binding.btnCreate.setOnClickListener {
            when {
                binding.editTextNote.text.toString().isBlank() ->
                    Toast.makeText(
                        context,
                        getString(R.string.empty_note_error),
                        Toast.LENGTH_SHORT
                    ).show()
                note != null && binding.editTextNote.text.toString() == note?.text ->
                    Toast.makeText(context, getString(R.string.same_note_error), Toast.LENGTH_SHORT)
                        .show()
                else ->
                    CoroutineScope(Dispatchers.IO).launch { notify(context) }
            }
        }
    }

    private fun displayAboutDialog(): AlertDialog {
        return AlertDialog.Builder(this)
            .setMessage(String.format(getString(R.string.about_me_message), BuildConfig.VERSION_NAME))
            .setPositiveButton(R.string.ok, null)
            .setNeutralButton(R.string.github) { _: DialogInterface, _: Int ->
                val githubUrl = "https://github.com/alibardide5124/notal.git"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
                startActivity(intent)
            }
            .create()
    }

    @SuppressLint("MissingPermission")
    private suspend fun notify(context: Context) {
        val id = note?.id ?: prefs.getInt(Constants.KEY_ID, 0)
        val notification = NotificationUtil(this)
            .provideNotification(id, binding.editTextNote.text.toString())

        NotificationManagerCompat.from(context).notify(id, notification)
        noteDao.upsertNote(Note(id, binding.editTextNote.text.toString()))

        if (note != null) {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(
                    context,
                    getString(R.string.changes_applied),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }

        prefs.edit().putInt(Constants.KEY_ID, id+1).apply()
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(
                context,
                getString(R.string.notification_created),
                Toast.LENGTH_SHORT
            ).show()
            binding.editTextNote.setText("")
        }
        this.currentFocus?.let { view ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun updateNoteFromIntent() {
        if (intent.hasExtra(Constants.KEY_EDIT)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                note = intent.getSerializableExtra(Constants.KEY_EDIT, Note::class.java)
            else
                @Suppress("DEPRECATION")
                note = intent.getSerializableExtra(Constants.KEY_EDIT) as Note
            binding.editTextNote.setText(note?.text!!)
            binding.btnCreate.text = getString(R.string.btn_main_edit)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        when {
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.POST_NOTIFICATIONS
            )
            -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.permission_ration_title)
                    .setMessage(R.string.permission_ration_message)
                    .setPositiveButton(R.string.cancel, null)
                    .setNegativeButton(R.string.ok) { _: DialogInterface, _: Int ->
                        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                            if (isGranted.not()) finish()
                        }.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    .create()
            }
            else ->
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                    if (isGranted.not()) finish()
                }.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun hasNotificationPermission() =
        ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

}