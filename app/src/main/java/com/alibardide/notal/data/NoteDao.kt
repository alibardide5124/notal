package com.alibardide.notal.data

import androidx.room.*

@Dao
interface NoteDao {
    @Upsert
    suspend fun upsertNote(note: Note)
    @Delete
    suspend fun deleteNote(note: Note)
    @Query("SELECT * FROM notes")
    suspend fun getAllNotes(): List<Note>
}