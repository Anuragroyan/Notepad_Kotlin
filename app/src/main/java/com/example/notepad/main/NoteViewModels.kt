package com.example.notepad.main;

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.firebase.NoteRepository
import com.example.notepad.model.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NoteViewModels : ViewModel() {
    private val repository = NoteRepository()
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    init { refresh() }

    private fun refresh() {
        viewModelScope.launch {
            _notes.value = repository.getAllNotes()
        }
    }

    fun addNote(note: Note) = viewModelScope.launch {
        repository.addNote(note)
        refresh()
    }

    fun updateNote(note: Note) = viewModelScope.launch {
        repository.updateNote(note)
        refresh()
    }

    fun deleteNote(id: String) = viewModelScope.launch {
        repository.deleteNote(id)
        refresh()
    }
}