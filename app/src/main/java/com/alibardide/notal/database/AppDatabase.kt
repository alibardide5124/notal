package com.alibardide.notal.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import com.alibardide.notal.model.Note

class AppDatabase(
    context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION
) {

    // Required values
    companion object {
        const val DATABASE_NAME = "notal"
        const val DATABASE_VERSION = 1

        const val TABLE_NOTE = "notes"
        const val NOTE_ID = "id"
        const val NOTE_TEXT = "text"
    }
    // Create table
    override fun onCreate(p0: SQLiteDatabase?) {
        p0?.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_NOTE (" +
                "$NOTE_ID INTEGER PRIMARY KEY," +
                "$NOTE_TEXT TEXT);")
    }
    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {}
    // Save note object to database
    fun save(entity: Note): Boolean {
        val db = writableDatabase
        val values = contentValuesOf()
        values.put(NOTE_TEXT, entity.text)
        val result = db.insert(TABLE_NOTE, null, values)
        db.close()
        // return if operation was successful
        return result > 0
    }
    // Update exist note
    fun update(id: String, entity: Note): Boolean {
        val db = writableDatabase
        val values = contentValuesOf()
        values.put(NOTE_TEXT, entity.text)
        val result = db.update(TABLE_NOTE, values, "$NOTE_ID = ?", arrayOf(id))
        db.close()
        // Return if operation was successful
        return result > 0
    }
    // Find all exist notes
    fun findNotes() : List<Note> {
        val db = readableDatabase
        val data = ArrayList<Note>()
        // Get notes from database
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NOTE", null)
        // Put notes in list
        if (cursor.moveToFirst()) {
            do {
                data.add(Note(
                    cursor.getString(cursor.getColumnIndex(NOTE_ID)).toInt(),
                    cursor.getString(cursor.getColumnIndex(NOTE_TEXT))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return data
    }
    // Delete exist note
    fun delete(id: String): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_NOTE, "$NOTE_ID = ?", arrayOf(id))
        db.close()
        // Return if operation was successful
        return result > 0
    }

}