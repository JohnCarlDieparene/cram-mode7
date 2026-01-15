    package com.labactivity.crammode

    import android.content.Intent
    import android.os.Bundle
    import android.util.Log
    import android.widget.Toast
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.setContent
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.text.KeyboardOptions
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.text.input.KeyboardType
    import androidx.compose.ui.unit.dp
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.lifecycleScope
    import androidx.lifecycle.viewmodel.compose.viewModel
    import com.labactivity.crammode.model.QuizQuestion
    import com.labactivity.crammode.utils.QuizUtils
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.suspendCancellableCoroutine
    import retrofit2.Call
    import retrofit2.Callback
    import retrofit2.Response
    import kotlin.coroutines.resume
    import kotlin.coroutines.resumeWithException
    import androidx.activity.compose.rememberLauncherForActivityResult
    import androidx.compose.foundation.rememberScrollState
    import androidx.activity.result.contract.ActivityResultContracts
    import android.net.Uri
import androidx.compose.foundation.verticalScroll
    import androidx.core.content.FileProvider
    import java.io.File
    import android.content.Context
    import kotlinx.coroutines.CoroutineScope
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.automirrored.filled.ArrowBack
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.foundation.text.BasicTextField
    import androidx.compose.foundation.background
    import androidx.compose.ui.Alignment
    import androidx.compose.material.icons.filled.Clear
    import androidx.compose.material.icons.filled.FormatListNumbered


    class QuizActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContent {
                MaterialTheme {
                    QuizScreen()
                }
            }
        }
    }

    // ----------------- Compose UI -----------------
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
    @Composable
    fun QuizScreen(
        vm: QuizActivityVM = viewModel(),
        context: Context = LocalContext.current,
        scope: CoroutineScope = rememberCoroutineScope()
    ) {
        var numQuestionsInput by remember { mutableStateOf("5") }
        var language by remember { mutableStateOf("English") }
        val scrollState = rememberScrollState()

        // --- Pickers ---
        val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { scope.launch { extractTextFromImageAsync(context, it) { text -> vm.inputText += if (vm.inputText.isEmpty()) text else "\n$text" } } }
        }

        val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { scope.launch { handleFileImport(context, it) { text -> vm.inputText += if (vm.inputText.isEmpty()) text else "\n$text" } } }
        }

        val cameraUri = remember { mutableStateOf<Uri?>(null) }
        val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                cameraUri.value?.let { scope.launch { extractTextFromImageAsync(context, it) { text -> vm.inputText += if (vm.inputText.isEmpty()) text else "\n$text" } } }
            }
        }

        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = { Text("Quiz Generator") },
                    navigationIcon = {
                        IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (vm.inputText.isBlank()) {
                            Toast.makeText(context, "No text to generate quiz", Toast.LENGTH_SHORT).show()
                            return@ExtendedFloatingActionButton
                        }

                        val n = numQuestionsInput.toIntOrNull()
                        if (n == null || n !in 1..10) {
                            Toast.makeText(context, "Enter a number from 1–10", Toast.LENGTH_SHORT).show()
                            return@ExtendedFloatingActionButton
                        }

                        scope.launch {
                            vm.isLoading = true
                            vm.generateQuiz(n, language)
                            vm.isLoading = false

                            if (vm.quizQuestions.isNotEmpty()) {
                                val intent = Intent(context, QuizViewerActivity::class.java)
                                intent.putParcelableArrayListExtra("quiz_list", ArrayList(vm.quizQuestions))
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, "No quiz questions generated", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    content = { Text(if (vm.isLoading) "Generating..." else "Generate Quiz") }
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
                        Text("Type or import notes", style = MaterialTheme.typography.titleMedium)
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
                                    "Paste notes here, import from PDF or images…",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }

                            if (vm.inputText.isNotEmpty()) {
                                IconButton(
                                    onClick = { vm.inputText = "" ; vm.quizQuestions = emptyList() },
                                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear text", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                // ---------- Number & Language Card ----------
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
                        // Number of Questions Input (smaller weight)
                        OutlinedTextField(
                            value = numQuestionsInput,
                            onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 2) numQuestionsInput = it },
                            label = { Text("Questions (1–10)") },
                            leadingIcon = { Icon(Icons.Default.FormatListNumbered, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.weight(0.4f) // smaller portion of the row
                        )

                        // Language Dropdown (larger weight)
                        LanguageDropdown(
                            selected = language,
                            onSelected = { language = it },
                            modifier = Modifier.weight(0.6f) // takes remaining space
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




    // ----------------- ViewModel -----------------
    class QuizActivityVM : ViewModel() {
        var inputText by mutableStateOf("")
        var quizQuestions by mutableStateOf<List<QuizQuestion>>(emptyList())
        var isLoading by mutableStateOf(false)

        suspend fun generateQuiz(count: Int, language: String) {
            if (inputText.isBlank()) return

            isLoading = true
            try {
                val request = buildCohereRequest(count, language)
                val rawResponse = sendChatRequest(request)
                quizQuestions = QuizUtils.parseQuizQuestions(rawResponse)
                Log.d("QuizVM", "Parsed ${quizQuestions.size} questions")
            } catch (e: Exception) {
                Log.e("QuizVM", "Failed to generate quiz", e)
            } finally {
                isLoading = false
            }
        }

        private fun buildCohereRequest(count: Int, language: String): ChatRequest {
            val systemPrompt = if (language == "Filipino") {
                """
        Ikaw ay isang AI quiz generator. Gumawa ng eksaktong $count multiple-choice na tanong mula sa ibinigay na teksto.
        Gamitin ang eksaktong format na ito:
        Tanong: <question text>
        A. <choice1>
        B. <choice2>
        C. <choice3>
        D. <choice4>
        Sagot: <tamang letra A-D>
        """.trimIndent()
            } else {
                """
        You are an AI quiz generator. Generate exactly $count multiple-choice questions from the given text.
        If the text is not in English, translate it to English first.
        Use this exact format:
        Question: <question text>
        A. <choice1>
        B. <choice2>
        C. <choice3>
        D. <choice4>
        Answer: <correct letter A-D>
        """.trimIndent()
            }

            val userPrompt = if (language == "Filipino") {
                "Gumawa ng $count tanong mula sa tekstong ito:\n\n$inputText\n\nSundin ang format."
            } else {
                "Generate $count questions from this text:\n\n$inputText\n\nTranslate to English if needed, and follow the format exactly."
            }

            return ChatRequest(
                messages = listOf(
                    ChatMessage("system", listOf(MessageContent(text = systemPrompt))),
                    ChatMessage("user", listOf(MessageContent(text = userPrompt)))
                )
            )
        }





        private suspend fun sendChatRequest(request: ChatRequest): String =
            suspendCancellableCoroutine { cont ->
                CohereClient.api.chat("Bearer ${BuildConfig.COHERE_API_KEY}", request)
                    .enqueue(object : retrofit2.Callback<ChatResponse> {
                        override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                            val reply = response.body()?.message?.content
                                ?.joinToString("\n") { it.text.trim() }
                                ?.takeIf { it.isNotBlank() } ?: ""
                            cont.resume(reply)
                        }

                        override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                            cont.resumeWithException(t)
                        }
                    })
            }
    }
