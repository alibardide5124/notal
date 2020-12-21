package com.alibardide.notal.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import com.alibardide.notal.model.Note

class AppDatabase(
    context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION
) {

    companion object {
        const val DATABASE_NAME = "notal"
        const val DATABASE_VERSION = 1

        const val TABLE_NOTE = "notes"
        const val NOTE_ID = "id"
        const val NOTE_TEXT = "text"
    }

    private val data: MutableList<Note> = ArrayList()
    private val contentValues = contentValuesOf()

    override fun onCreate(p0: SQLiteDatabase?) {
        p0?.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_NOTE (" +
                "$NOTE_ID INTEGER PRIMARY KEY," +
                "$NOTE_TEXT TEXT);")
    }
    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {}

    fun save(entity: Note): Boolean {
        val db = writableDatabase
        contentValues.clear()
        contentValues.put(NOTE_TEXT, entity.text)
        val result = db.insert(TABLE_NOTE, null, contentValues)
        db.close()
        return result > 0
    }
    fun update(id: String, entity: Note): Boolean {
        val db = writableDatabase
        contentValues.clear()
        contentValues.put(NOTE_TEXT, entity.text)
        val result = db.update(TABLE_NOTE, contentValues, "$NOTE_ID = ?", arrayOf(id))
        db.close()
        return result > 0
    }
    fun delete(id: String): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_NOTE, "$NOTE_ID = ?", arrayOf(id))
        db.close()
        return result > 0
    }

}