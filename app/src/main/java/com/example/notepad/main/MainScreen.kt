package com.example.notepad.main

import android.graphics.Color.parseColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.notepad.model.Note
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.graphics.toColorInt

@Composable
fun MainScreen(viewModel: NoteViewModels = viewModel()) {
    val notes by viewModel.notes.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var selectedNoteId by remember { mutableStateOf<String?>(null) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var colorHex by remember { mutableStateOf("#FFFFEB3B") }
    var search by remember { mutableStateOf("") }

    fun isValidHex(hex: String): Boolean =
        try {
            parseColor(hex)
            true
        } catch (_: Exception) {
            false
        }

    val filtered = notes.filter {
        it.title.contains(search, true) ||
                it.content.contains(search, true) ||
                it.tags.any { tag -> tag.contains(search, true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 36.dp)
            .verticalScroll(rememberScrollState())
    ) {

        // --- Search ---
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            label = { Text("🔍 Search Notes") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // --- Note Inputs ---
        Text("📝 Create a New Note", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Content") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = tags,
            onValueChange = { tags = it },
            label = { Text("Tags (comma-separated)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = colorHex,
            onValueChange = { colorHex = it },
            label = { Text("Note Color (#RRGGBB)") },
            modifier = Modifier.fillMaxWidth(),
            isError = !isValidHex(colorHex)
        )

        if (!isValidHex(colorHex)) {
            Text(
                text = "⚠️ Invalid hex color",
                color = ComposeColor.Red,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(Modifier.height(12.dp))

        // --- Add Button --- or Edit Button ---
        Button(
            enabled = title.isNotBlank() && isValidHex(colorHex),
            onClick = {
                coroutineScope.launch {
                    // Check for duplicate title (if adding new)
                    if (selectedNoteId == null && notes.any { it.title == title }) {
                        // Show error (can be improved using Snackbar or Toast)
                        return@launch
                    }

                    if (selectedNoteId == null) {
                        // ADD new note
                        viewModel.addNote(
                            Note(
                                title = title,
                                content = content,
                                colorHex = colorHex,
                                tags = tags.split(",").map { it.trim() }.filter(String::isNotBlank)
                            )
                        )
                    } else {
                        // EDIT existing note
                        viewModel.updateNote(
                            Note(
                                id = selectedNoteId!!,
                                title = title,
                                content = content,
                                colorHex = colorHex,
                                tags = tags.split(",").map { it.trim() }.filter(String::isNotBlank)
                            )
                        )
                        selectedNoteId = null // clear editing state
                    }

                    // Reset fields after add/edit
                    title = ""; content = ""; tags = ""; colorHex = "#FFFFFF"
                }
            }
        ) {
            Text(if (selectedNoteId == null) "Add Note" else "Update Note")
        }


        Spacer(Modifier.height(16.dp))

        // --- Notes List ---
        Text("📋 Notes", style = MaterialTheme.typography.titleMedium)

        filtered.forEach { note ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = runCatching { ComposeColor(note.colorHex.toColorInt()) }
                        .getOrElse { ComposeColor.LightGray }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(note.title, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(note.content, style = MaterialTheme.typography.bodyMedium)

                    if (note.tags.isNotEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        Row(modifier = Modifier.wrapContentWidth()) {
                            note.tags.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .padding(end = 6.dp)
                                        .background(
                                            color = ComposeColor.White.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(tag, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Row {
                        OutlinedButton(
                            onClick = { coroutineScope.launch { viewModel.deleteNote(note.id) } },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Delete")
                        }

                        Spacer(Modifier.width(8.dp))

                        Button(onClick = {
                            selectedNoteId = note.id
                            title = note.title
                            content = note.content
                            tags = note.tags.joinToString(",")
                            colorHex = note.colorHex
                        }) {
                            Text("Edit")
                        }

                    }
                }
            }
        }
    }
}


@Composable
fun Viewmodel() {
    TODO("Not yet implemented")
}