package com.alibardide.notal.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey
    val id: Int,
    val text: String
) : Serializable