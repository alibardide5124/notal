package com.alibardide.notal

import java.io.Serializable

data class Note(
    val id: Int,
    val text: String
) : Serializable