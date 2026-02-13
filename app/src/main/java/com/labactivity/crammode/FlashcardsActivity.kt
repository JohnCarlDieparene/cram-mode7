package com.labactivity.crammode

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.labactivity.crammode.utils.FlashcardUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.Scanner

// -------------------- ACTIVITY --------------------
class FlashcardsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val vm: FlashcardActivityVM = viewModel()
                val context = LocalContext.current
                val scope = rememberCoroutineScope()




                FlashcardsScreen(
                    vm = vm,
                    context = context,
                    scope = scope,

                )
            }
        }
    }

    private fun textFromUri(context: Context, uri: android.net.Uri): String? =
        context.contentResolver.openInputStream(uri)?.use { Scanner(it).useDelimiter("\\A").nextOrNull() }

    private fun Scanner.nextOrNull(): String? = if (hasNext()) next() else null
}

// -------------------- SCREEN --------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    vm: FlashcardActivityVM,
    context: Context,
    scope: CoroutineScope
)
 {
    var flashcardCountInput by remember { mutableStateOf("5") }
    var language by remember { mutableStateOf("English") }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Flashcards Generator") },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            // -------------------- FAB CLICK FIX --------------------
            ExtendedFloatingActionButton(
                onClick = {
                    if (vm.inputText.isBlank()) {
                        Toast.makeText(context, "No text to generate flashcards", Toast.LENGTH_SHORT).show()
                        return@ExtendedFloatingActionButton
                    }

                    val count = flashcardCountInput.toIntOrNull()
                    if (count == null || count !in 1..10) {
                        Toast.makeText(context, "Enter a number from 1 to 10", Toast.LENGTH_SHORT).show()
                        return@ExtendedFloatingActionButton
                    }

                    vm.isLoading = true
                    scope.launch {
                        vm.generateFlashcards(count, language)

                        // ✅ Use ViewModel function instead of undefined saveFlashcardHistory()
                        if (vm.flashcards.isNotEmpty()) {
                            vm.saveHistory() // <- FIXED
                            val intent = Intent(context, FlashcardViewerActivity::class.java)
                            intent.putExtra("flashcards", ArrayList(vm.flashcards))
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, "No flashcards generated.", Toast.LENGTH_LONG).show()
                        }
                        vm.isLoading = false
                    }
                },
                content = { Text(if (vm.isLoading) "Generating..." else "Generate Flashcards") }
            )


        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ---------- Input Card ----------
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Type or import text", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    Box {
                        BasicTextField(
                            value = vm.inputText,
                            onValueChange = { vm.inputText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                        )

                        if (vm.inputText.isEmpty()) {
                            Text(
                                text = "Paste notes here, import from PDF, or type manually…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        if (vm.inputText.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    vm.inputText = ""
                                    vm.flashcards = emptyList()
                                },
                                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear text", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // ---------- Flashcard Count & Language ----------
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = flashcardCountInput,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() } && newValue.length <= 2) flashcardCountInput = newValue
                        },
                        label = { Text("Cards (1–10)") },
                        leadingIcon = { Icon(Icons.Default.FormatListNumbered, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.width(120.dp) // fixed width so it doesn't eat too much space
                    )

                    LanguageDropdown(
                        selected = language,
                        onSelected = { language = it },
                        modifier = Modifier.weight(1f) // now has enough space for full text
                    )
                }
            }


            // ---------- Import Chips ----------
            ImportChips(context, scope) { newText ->
                vm.inputText += if (vm.inputText.isEmpty()) newText else "\n$newText"
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageDropdown(selected: String, onSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    val options = listOf("English", "Filipino")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Language Output") },
            leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            singleLine = true
        )

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelected(option); expanded = false })
            }
        }
    }
}

// -------------------- VIEWMODEL --------------------
class FlashcardActivityVM : ViewModel() {
    var inputText by mutableStateOf("")
    var flashcards by mutableStateOf<List<Flashcard>>(emptyList())
    var isLoading by mutableStateOf(false)

    suspend fun generateFlashcards(count: Int = 5, language: String = "English") {
        val repo = FlashcardRepository()
        val reply = repo.generateFlashcards(inputText, count, language)
        flashcards = FlashcardUtils.parseFlashcards(reply)
        isLoading = false
    }

    fun saveHistory() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val firestore = FirebaseFirestore.getInstance()
        val data = hashMapOf(
            "uid" to user.uid,
            "type" to "flashcards",
            "inputText" to inputText,
            "resultFlashcards" to flashcards.map {
                mapOf("question" to it.question, "answer" to it.answer)
            },
            "timestamp" to System.currentTimeMillis()
        )
        firestore.collection("study_history").add(data)
    }

}

// -------------------- REPOSITORY --------------------
class FlashcardRepository {
    suspend fun generateFlashcards(
        input: String,
        count: Int = 5,
        language: String = "English"
    ): String =
        withContext(Dispatchers.IO) {
            try {
                val systemPrompt =
                    "You are an AI study assistant that creates flashcards to support active recall and efficient review."

                val userPrompt = """
Create exactly $count flashcards based on the **key concepts and important ideas** from the text below.

Strict formatting rules:
1. Each flashcard must start with "Q:" on a NEW line, followed by a clear and concise question.
2. The answer must start with "A:" on the NEXT line and directly answer the question.
3. Each Q&A pair must represent ONE flashcard only.
4. Do NOT merge multiple questions or answers.
5. Do NOT add numbering, headings, or extra explanations.
6. Generate EXACTLY $count flashcards.
7. Write in $language.

Focus on:
- Main concepts
- Definitions
- Relationships between ideas
- Important facts useful for review

Text:
$input
""".trimIndent()



                val request = ChatRequest(
                    messages = listOf(
                        ChatMessage(
                            role = "system",
                            content = listOf(MessageContent(text = systemPrompt))
                        ),
                        ChatMessage(
                            role = "user",
                            content = listOf(MessageContent(text = userPrompt))
                        )
                    )
                )

                val response =
                    CohereClient.api.chat("Bearer ${BuildConfig.COHERE_API_KEY}", request).execute()
                if (response.isSuccessful) response.body()?.message?.content?.joinToString("\n") { it.text }
                    ?: "" else ""
            } catch (e: Exception) {
                ""
            }
        }

    // -------------------- HELPER: SAVE HISTORY --------------------
    fun saveFlashcardHistory(inputText: String, flashcards: List<Flashcard>) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val firestore = FirebaseFirestore.getInstance()
        val data = hashMapOf(
            "uid" to user.uid,
            "type" to "flashcards",
            "inputText" to inputText,
            "resultFlashcards" to flashcards.map {
                mapOf(
                    "question" to it.question,
                    "answer" to it.answer
                )
            },
            "timestamp" to System.currentTimeMillis()
        )
        firestore.collection("study_history").add(data)
    }
}