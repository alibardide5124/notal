package com.alibardide.notal.data

import androidx.room.*

@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract val noteDao: NoteDao
}
