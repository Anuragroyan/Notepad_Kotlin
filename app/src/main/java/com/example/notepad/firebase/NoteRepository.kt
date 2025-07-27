package com.example.notepad.firebase

import com.example.notepad.model.Note
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

class NoteRepository {
    private val db = FirebaseFirestore.getInstance()
    private val notesRef = db.collection("notes")

    suspend fun addNote(note: Note) {
        val id = UUID.randomUUID().toString()
        notesRef.document(id).set(note.copy(id = id)).await()
    }

    suspend fun updateNote(note: Note) {
        notesRef.document(note.id).set(note).await()
    }

    suspend fun deleteNote(id: String) {
        notesRef.document(id).delete().await()
    }

    suspend fun getAllNotes(): List<Note> =
        notesRef.get().await().toObjects(Note::class.java)
}