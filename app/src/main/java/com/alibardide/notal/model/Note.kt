package com.alibardide.notal.model

import java.io.Serializable

data class Note(
    val id: Int,
    val text: String
) : Serializable