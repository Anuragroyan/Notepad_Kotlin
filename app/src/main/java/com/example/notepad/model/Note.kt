package com.example.notepad.model

data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val colorHex: String = "#FFFFFF",
    val tags: List<String> = listOf()
)

