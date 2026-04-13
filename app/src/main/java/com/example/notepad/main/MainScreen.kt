package com.example.notepad.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notepad.model.Note
import kotlinx.coroutines.launch

// ── Theme colors ──────────────────────────────────────────────
val DarkBackground = Color(0xFF2A2A2A)
val SearchBoxBg = Color(0xFF383838)
val SearchBoxBorder = Color(0xFF444444)
val TextPrimary    = Color(0xFFEFEFEF)
val TextHint       = Color(0xFF666666)
val FabBg = Color(0xFF383838)

// ── Per-note color palette ─────────────────────────────────────
data class NoteTheme(
    val cardBg: Color,
    val accent: Color,
    val barColor: Color,
    val wireDark: Color,
    val wireLight: Color,
    val tagBg: Color,
    val tagText: Color,
)

val noteThemes = listOf(
    // Yellow
    NoteTheme(Color(0xFFFFF176), Color(0xFFF9A825), Color(0xFFF9A825), Color(0xFFC47F00), Color(0xFFFFE082), Color(0xFFFFF9C4), Color(0xFF7A6000)),
    // Teal
    NoteTheme(Color(0xFFB2EBF2), Color(0xFF00838F), Color(0xFF00838F), Color(0xFF005F6B), Color(0xFF4DD0E1), Color(0xFFE0F7FA), Color(0xFF00363A)),
    // Purple
    NoteTheme(Color(0xFFCE93D8), Color(0xFF6A1B9A), Color(0xFF6A1B9A), Color(0xFF4A0072), Color(0xFFE040FB), Color(0xFFF3E5F5), Color(0xFF4A148C)),
    // Green
    NoteTheme(Color(0xFFA5D6A7), Color(0xFF2E7D32), Color(0xFF2E7D32), Color(0xFF1B5E20), Color(0xFF69F0AE), Color(0xFFE8F5E9), Color(0xFF1B5E20)),
    // Coral
    NoteTheme(Color(0xFFFFAB91), Color(0xFFBF360C), Color(0xFFBF360C), Color(0xFF7F2500), Color(0xFFFFCCBC), Color(0xFFFBE9E7), Color(0xFF7F2500)),
)

fun themeForIndex(index: Int) = noteThemes[index % noteThemes.size]

// ── Main Screen ────────────────────────────────────────────────
@Composable
fun MainScreen(viewModel: NoteViewModels = viewModel()) {
    val notes by viewModel.notes.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var selectedNoteId by remember { mutableStateOf<String?>(null) }
    var title     by remember { mutableStateOf("") }
    var content   by remember { mutableStateOf("") }
    var tags      by remember { mutableStateOf("") }
    var colorHex  by remember { mutableStateOf("#FFFFEB3B") }
    var search    by remember { mutableStateOf("") }

    fun isValidHex(hex: String) = try { hex.toColorInt(); true } catch (_: Exception) { false }

    val filtered = notes.filter {
        it.title.contains(search, true) ||
                it.content.contains(search, true) ||
                it.tags.any { tag -> tag.contains(search, true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
            .padding(top = 36.dp)
            .verticalScroll(rememberScrollState())
    ) {

        // ── Title ──
        Text(
            text = "My Notes",
            fontSize = 22.sp,
            color = TextPrimary,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // ── Search Box ──
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            label = { Text("Search notes...", color = TextHint) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = SearchBoxBorder,
                unfocusedBorderColor = SearchBoxBorder,
                focusedContainerColor   = SearchBoxBg,
                unfocusedContainerColor = SearchBoxBg,
                cursorColor          = TextPrimary,
                focusedTextColor     = TextPrimary,
                unfocusedTextColor   = TextPrimary,
                focusedLabelColor    = TextHint,
                unfocusedLabelColor  = TextHint,
            )
        )

        Spacer(Modifier.height(20.dp))

        // ── Create / Edit Form ──
        Text(
            text = if (selectedNoteId == null) "Create a New Note" else "Edit Note",
            fontSize = 16.sp,
            color = TextPrimary,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(8.dp))

        listOf(
            Triple(title,   { v: String -> title = v },   "Title"),
            Triple(content, { v: String -> content = v }, "Content"),
            Triple(tags,    { v: String -> tags = v },    "Tags (comma-separated)"),
        ).forEach { (value, onChange, label) ->
            OutlinedTextField(
                value = value,
                onValueChange = onChange,
                label = { Text(label, color = TextHint) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = SearchBoxBorder,
                    unfocusedBorderColor    = SearchBoxBorder,
                    focusedContainerColor   = SearchBoxBg,
                    unfocusedContainerColor = SearchBoxBg,
                    cursorColor             = TextPrimary,
                    focusedTextColor        = TextPrimary,
                    unfocusedTextColor      = TextPrimary,
                    focusedLabelColor       = TextHint,
                    unfocusedLabelColor     = TextHint,
                )
            )
            Spacer(Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = colorHex,
            onValueChange = { colorHex = it },
            label = { Text("Note Color (#RRGGBB)", color = TextHint) },
            isError = !isValidHex(colorHex),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = SearchBoxBorder,
                unfocusedBorderColor    = SearchBoxBorder,
                focusedContainerColor   = SearchBoxBg,
                unfocusedContainerColor = SearchBoxBg,
                cursorColor             = TextPrimary,
                focusedTextColor        = TextPrimary,
                unfocusedTextColor      = TextPrimary,
                focusedLabelColor       = TextHint,
                unfocusedLabelColor     = TextHint,
            )
        )
        if (!isValidHex(colorHex)) {
            Text("⚠️ Invalid hex color", color = Color.Red, style = MaterialTheme.typography.labelSmall)
        }

        Spacer(Modifier.height(12.dp))

        // ── Add / Update Button ──
        Button(
            enabled = title.isNotBlank() && isValidHex(colorHex),
            onClick = {
                coroutineScope.launch {
                    if (selectedNoteId == null && notes.any { it.title == title }) return@launch
                    if (selectedNoteId == null) {
                        viewModel.addNote(Note(
                            title = title, content = content, colorHex = colorHex,
                            tags = tags.split(",").map { it.trim() }.filter(String::isNotBlank)
                        ))
                    } else {
                        viewModel.updateNote(Note(
                            id = selectedNoteId!!, title = title, content = content,
                            colorHex = colorHex,
                            tags = tags.split(",").map { it.trim() }.filter(String::isNotBlank)
                        ))
                        selectedNoteId = null
                    }
                    title = ""; content = ""; tags = ""; colorHex = "#FFFFFF"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FabBg)
        ) {
            Text(
                if (selectedNoteId == null) "Add Note" else "Update Note",
                color = TextPrimary
            )
        }

        Spacer(Modifier.height(24.dp))

        // ── Notes List ──
        Text("Notes", fontSize = 16.sp, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))

        filtered.forEachIndexed { index, note ->
            val theme = themeForIndex(index)
            NoteCard(
                note = note,
                theme = theme,
                onDelete = { coroutineScope.launch { viewModel.deleteNote(note.id) } },
                onEdit = {
                    selectedNoteId = note.id
                    title    = note.title
                    content  = note.content
                    tags     = note.tags.joinToString(",")
                    colorHex = note.colorHex
                }
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Note Card ──────────────────────────────────────────────────
@Composable
fun NoteCard(
    note: Note,
    theme: NoteTheme,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
) {
    Column {
        // Spiral bar
        SpiralBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            barColor   = theme.barColor,
            wireDark   = theme.wireDark,
            wireLight  = theme.wireLight,
        )

        // Paper body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                .background(theme.cardBg)
                .then(
                    Modifier.offset(x = 4.dp, y = 4.dp) // shadow trick via outer Box
                )
        ) {
            // Ruled lines
            Canvas(modifier = Modifier.matchParentSize()) {
                val lineSpacing = 28.dp.toPx()
                var y = lineSpacing
                while (y < size.height) {
                    drawLine(
                        color = Color.Black.copy(alpha = 0.07f),
                        start = Offset(0f, y),
                        end   = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                    y += lineSpacing
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text  = note.title,
                    fontSize = 15.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    color = theme.accent
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = note.content,
                    fontSize = 13.sp,
                    color = Color.Black.copy(alpha = 0.58f),
                    lineHeight = 20.sp
                )

                if (note.tags.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        note.tags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .background(theme.tagBg, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("#$tag", fontSize = 11.sp, color = theme.tagText,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB41E1E)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFB41E1E).copy(alpha = 0.4f))
                    ) { Text("Delete", fontSize = 12.sp) }

                    Button(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.5f)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, theme.accent)
                    ) { Text("Edit", fontSize = 12.sp, color = theme.accent) }
                }
            }
        }
    }
}

// ── Spiral Bar (Canvas) ────────────────────────────────────────
@Composable
fun SpiralBar(
    modifier: Modifier = Modifier,
    barColor: Color,
    wireDark: Color,
    wireLight: Color,
) {
    Canvas(modifier = modifier) {
        val barH    = 20.dp.toPx()
        val barTop  = size.height - barH
        val coilW   = 28.dp.toPx()
        val gap     = 5.dp.toPx()
        val padX    = 10.dp.toPx()
        val count   = ((size.width - padX * 2) / (coilW + gap)).toInt()
        val total   = count * (coilW + gap) - gap
        val startX  = (size.width - total) / 2f

        // Black bar
        drawRoundRect(
            color        = barColor,
            topLeft      = Offset(0f, barTop),
            size         = Size(size.width, barH),
            cornerRadius = CornerRadius(6.dp.toPx())
        )

        repeat(count) { i ->
            val cx     = startX + i * (coilW + gap) + coilW / 2f
            val hw     = coilW * 0.42f
            val loopTop = 4.dp.toPx()
            val loopBot = barTop + 10.dp.toPx()

            // Main wire coil path
            val wirePath = Path().apply {
                moveTo(cx - hw, barTop + 4.dp.toPx())
                cubicTo(cx - hw, loopTop, cx + hw, loopTop, cx + hw, barTop + 4.dp.toPx())
                cubicTo(cx + hw, loopBot, cx - hw, loopBot, cx - hw, barTop + 4.dp.toPx())
                close()
            }
            drawPath(wirePath, color = wireDark, style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round))

            // Highlight
            val highlightPath = Path().apply {
                moveTo(cx - hw + 3.dp.toPx(), barTop + 2.dp.toPx())
                cubicTo(
                    cx - hw + 2.dp.toPx(), loopTop + 3.dp.toPx(),
                    cx + hw - 2.dp.toPx(), loopTop + 3.dp.toPx(),
                    cx + hw - 3.dp.toPx(), barTop + 2.dp.toPx()
                )
            }
            drawPath(highlightPath, color = wireLight.copy(alpha = 0.85f), style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round))

            // Bottom shadow
            val shadowPath = Path().apply {
                moveTo(cx - hw + 2.dp.toPx(), loopBot - 2.dp.toPx())
                cubicTo(
                    cx - hw + 2.dp.toPx(), loopBot + 6.dp.toPx(),
                    cx + hw - 2.dp.toPx(), loopBot + 6.dp.toPx(),
                    cx + hw - 2.dp.toPx(), loopBot - 2.dp.toPx()
                )
            }
            drawPath(shadowPath, color = wireDark.copy(alpha = 0.5f), style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))

            // Punched hole
            drawOval(
                color   = wireDark.copy(alpha = 0.5f),
                topLeft = Offset(cx - 4.5.dp.toPx(), barTop + 9.dp.toPx() - 3.5.dp.toPx()),
                size    = Size(9.dp.toPx(), 7.dp.toPx())
            )
        }
    }
}


@Composable
fun Viewmodel() {
    TODO("Not yet implemented")
}