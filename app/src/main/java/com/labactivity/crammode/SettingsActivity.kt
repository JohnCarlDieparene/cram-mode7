package com.labactivity.crammode

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                SettingsScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var remindersEnabled by remember { mutableStateOf(false) }

    // --- AI Preferences state ---
    var summaryLength by remember { mutableStateOf("Medium") }
    var quizDifficulty by remember { mutableStateOf("Medium") }
    var flashcardStyle by remember { mutableStateOf("Q&A") }
    var aiSuggestionsEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            // ---------------- STUDY ----------------
            SettingsSection("Study")

            SettingsSwitchItem(
                icon = Icons.Default.Notifications,
                title = "Study Reminders",
                checked = remindersEnabled,
                onCheckedChange = {
                    remindersEnabled = it
                    Toast.makeText(
                        context,
                        "Study reminders coming soon",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )

            Divider()

            // ---------------- AI ----------------
            SettingsSection("AI & Learning")

            // ---- AI Suggestions Toggle ----
            SettingsSwitchItem(
                icon = Icons.Default.AutoFixHigh,
                title = "AI Suggestions",
                checked = aiSuggestionsEnabled,
                onCheckedChange = {
                    aiSuggestionsEnabled = it
                    Toast.makeText(context, "AI suggestions ${if (it) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
                }
            )

            // ---- Summary Length ----
            SettingsDropdownItem(
                icon = Icons.Default.Article,
                title = "Summary Length",
                options = listOf("Short", "Medium", "Long"),
                selectedOption = summaryLength,
                onOptionSelected = {
                    summaryLength = it
                    Toast.makeText(context, "Summary length: $it", Toast.LENGTH_SHORT).show()
                }
            )

            // ---- Quiz Difficulty ----
            SettingsDropdownItem(
                icon = Icons.Default.Quiz,
                title = "Quiz Difficulty",
                options = listOf("Easy", "Medium", "Hard"),
                selectedOption = quizDifficulty,
                onOptionSelected = {
                    quizDifficulty = it
                    Toast.makeText(context, "Quiz difficulty: $it", Toast.LENGTH_SHORT).show()
                }
            )

            // ---- Flashcard Style ----
            SettingsDropdownItem(
                icon = Icons.Default.Style,
                title = "Flashcard Style",
                options = listOf("Q&A", "Fill-in-the-Blank"),
                selectedOption = flashcardStyle,
                onOptionSelected = {
                    flashcardStyle = it
                    Toast.makeText(context, "Flashcard style: $it", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun SettingsDropdownItem(
    icon: ImageVector,
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(16.dp))
            Text(title, modifier = Modifier.weight(1f))
            Text(selectedOption, fontWeight = FontWeight.SemiBold)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
fun SettingsNavItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            subtitle?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null)
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(16.dp))
        Text(title, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
