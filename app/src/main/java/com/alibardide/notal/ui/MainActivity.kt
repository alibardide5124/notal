package com.alibardide.notal.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.alibardide.notal.BuildConfig
import com.alibardide.notal.R
import com.alibardide.notal.data.Note
import com.alibardide.notal.data.NoteDao
import com.alibardide.notal.databinding.ActivityMainBinding
import com.alibardide.notal.Constants
import com.alibardide.notal.ui.theme.NotalTheme
import com.alibardide.notal.utils.NotificationUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var prefs: SharedPreferences

    @Inject
    lateinit var noteDao: NoteDao
    private lateinit var binding: ActivityMainBinding
    private lateinit var composeView: ComposeView

    private var note: Note? = null
    private var notes: SnapshotStateList<Note> = mutableStateListOf<Note>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val context = this@MainActivity

        composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@MainActivity))
            setContent {
                NotalTheme {
                    NoteList()
                }
            }
        }
        binding.hiddenContainer.addView(composeView)

        // Check if note sent from intent
        updateNoteFromIntent()
        NotificationUtil(this).createNotificationChannel()

        if (Constants.isAtLeastTiramisu())
            requestNotificationPermission()

        binding.notifPermission.setOnClickListener {
            openAppSettings()
        }
        binding.imageViewAbout.setOnClickListener {
            displayAboutDialog()
        }
        binding.layoutHistory.setOnClickListener {
            displayHistoryDialog()
        }
        binding.btnCreate.setOnClickListener {
            when {
                Constants.isAtLeastTiramisu() && !Constants.hasNotificationPermission(this) -> {
                    Toast.makeText(
                        context,
                        getString(R.string.notification_permission_denied),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                binding.editTextNote.text.toString().isBlank() ->
                    Toast.makeText(
                        context,
                        getString(R.string.empty_note_error),
                        Toast.LENGTH_SHORT
                    ).show()

                note != null && binding.editTextNote.text.toString() == note?.text ->
                    Toast.makeText(
                        context,
                        getString(R.string.same_note_error),
                        Toast.LENGTH_SHORT
                    ).show()

                else ->
                    CoroutineScope(Dispatchers.IO).launch {
                        notify(
                            context,
                            id = note?.id ?: prefs.getInt(Constants.KEY_ID, 0),
                            text = binding.editTextNote.text.toString()
                        )
                    }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Constants.isAtLeastTiramisu())
            when (Constants.hasNotificationPermission(this)) {
                true -> enableCreateButton()
                false -> disableCreateButton()
            }
    }

    private fun displayAboutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(String.format(getString(R.string.about_me_title), BuildConfig.VERSION_NAME))
            .setMessage(getString(R.string.about_me_message))
            .setPositiveButton(R.string.ok, null)
            .setNeutralButton(R.string.github) { _: DialogInterface, _: Int ->
                val githubUrl = "https://github.com/alibardide5124/notal.git"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
                startActivity(intent)
            }
            .create()
            .show()
    }

    private fun displayHistoryDialog() {
        (composeView.parent as? ViewGroup)?.removeView(composeView)
        CoroutineScope(Dispatchers.IO).launch {
            notes = noteDao.getAllNotes().toMutableStateList()
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.history))
            .setView(composeView)
            .setPositiveButton("Dismiss", null)
            .create()
            .show()
    }

    @Composable
    private fun NoteList() {
        val coroutineScope = rememberCoroutineScope()
        val notesR by rememberUpdatedState(notes)
        var selectedId by remember { mutableIntStateOf(-1) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
            ) {
                item {
                    if (notes.isEmpty()) {
                        Text(
                            text = "No notes found",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.animateItem()
                        )
                    }
                }
                items(notesR, key = { it.id }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .animateItem()
                            .clickable {
                                selectedId = when {
                                    selectedId != it.id -> it.id
                                    else -> -1
                                }
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = it.text,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                        AnimatedVisibility(selectedId == it.id) {
                            Row {
                                Icon(
                                    painter = painterResource(R.drawable.ic_publish),
                                    contentDescription = "Publish",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.clickable {
                                        coroutineScope.launch {
                                            notify(
                                                this@MainActivity,
                                                id = it.id,
                                                text = it.text
                                            )
                                        }
                                    }
                                )
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    painter = painterResource(R.drawable.ic_delete),
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.clickable {
                                        coroutineScope.launch {
                                            noteDao.deleteNote(it)
                                            notes.remove(it)
                                            NotificationManagerCompat
                                                .from(this@MainActivity)
                                                .cancel(it.id)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun notify(context: Context, id: Int, text: String) {
        val notification = NotificationUtil(this).provideNotification(id, text)

        NotificationManagerCompat.from(context).notify(id, notification)
        noteDao.upsertNote(Note(id, text))

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

        prefs.edit().putInt(Constants.KEY_ID, id + 1).apply()
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
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.permission_ration_title)
                    .setMessage(R.string.permission_ration_message)
                    .setNegativeButton(R.string.cancel) { _: DialogInterface, _: Int ->
                        disableCreateButton()
                    }
                    .setPositiveButton(R.string.ok) { _: DialogInterface, _: Int ->
                        openAppSettings()
                    }
                    .create()
                    .show()
            }

            else ->
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                    if (isGranted.not()) disableCreateButton()
                }.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.setData(uri)
        startActivity(intent)
    }

    private fun disableCreateButton() {
        binding.btnCreate.isEnabled = false
        binding.btnCreate.text = getString(R.string.notal_needs_notification_permission)
        binding.notifPermission.visibility = View.VISIBLE
    }

    private fun enableCreateButton() {
        binding.btnCreate.isEnabled = true
        binding.btnCreate.text = getString(R.string.btn_main)
        binding.notifPermission.visibility = View.GONE
    }

}