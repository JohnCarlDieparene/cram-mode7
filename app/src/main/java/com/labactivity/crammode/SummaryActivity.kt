package com.labactivity.crammode

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import retrofit2.awaitResponse
import uriToBitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
class SummaryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                SummaryScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var inputText by remember { mutableStateOf(TextFieldValue("")) }
    var summary by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var language by remember { mutableStateOf("English") }
    var length by remember { mutableStateOf("Medium") }
    var format by remember { mutableStateOf("Paragraph") }

    val scrollState = rememberScrollState()
    var previousScroll by remember { mutableStateOf(0) }
    var fabVisible by remember { mutableStateOf(true) }

    // Detect scroll direction to hide/show FAB
    LaunchedEffect(scrollState.value) {
        fabVisible = scrollState.value <= previousScroll || scrollState.value < 50
        previousScroll = scrollState.value
    }

    // Pickers
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            scope.launch {
                extractTextFromImageAsync(context, it) { text ->
                    inputText = TextFieldValue(inputText.text + "\n$text")
                }
            }
        }
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            scope.launch {
                handleFileImport(context, it) { text ->
                    inputText = TextFieldValue(inputText.text + "\n$text")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Summary") },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = fabVisible,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    text = { Text(if (isLoading) "Generating..." else "Generate Summary") },
                    icon = {
                        if (isLoading)
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                    },
                    onClick = {
                        if (inputText.text.isBlank()) {
                            Toast.makeText(context, "No text to summarize", Toast.LENGTH_SHORT).show()
                        } else {
                            isLoading = true
                            scope.launch {
                                val result = sendSummaryRequestAsync(
                                    input = inputText.text,
                                    language = language,
                                    length = length.lowercase(),
                                    format = format.lowercase()
                                )
                                summary = result
                                isLoading = false

                                if (result.isNotBlank() && !result.startsWith("Error")) {
                                    saveSummaryHistory(inputText.text, result)
                                }
                            }
                        }
                    },

                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Input Card
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Type or import text", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))

                        Box {
                            BasicTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )

                            if (inputText.text.isEmpty()) {
                                Text(
                                    text = "Paste notes, import from images or PDF, or type manually…",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }

                            if (inputText.text.isNotEmpty()) {
                                IconButton(
                                    onClick = { inputText = TextFieldValue(""); summary = "" },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear text",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Options Row
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    mainAxisSpacing = 12.dp,
                    crossAxisSpacing = 12.dp,
                    crossAxisAlignment = FlowCrossAxisAlignment.Start
                ) {
                    DropdownMenuWrapper("Language Output", listOf("English", "Filipino"), language) { language = it }
                    DropdownMenuWrapper("Length", listOf("Short", "Medium", "Long"), length) { length = it }
                    DropdownMenuWrapper("Format", listOf("Paragraph", "Bullet"), format) { format = it }
                }

                // Import Chips
                ImportChips(context, scope) { newText ->
                    inputText = TextFieldValue(inputText.text + "\n$newText")
                }


                // Summary Output

                if (summary.isNotEmpty()) {
                    Text("Summary", style = MaterialTheme.typography.titleMedium, fontSize = 20.sp)

                    // Summary Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(summary, fontSize = 16.sp)
                        }
                    }

                    // Copy Button below the Card
                    Spacer(modifier = Modifier.height(8.dp)) // small space between card and button
                    val context = LocalContext.current
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Summary", summary)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Summary copied!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.align(Alignment.End) // button aligned to the right
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Summary")
                    }
                }

            }


            }
        }
    }




// ------------------ Helper Composables ------------------

@Composable
fun DropdownMenuWrapper(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        OutlinedCard(
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(selected, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSelect(option); expanded = false }
                )
            }
        }
    }
}

@Composable
fun ImportChips(
    context: Context,
    scope: CoroutineScope,
    onTextExtracted: (String) -> Unit
) {
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { scope.launch { extractTextFromImageAsync(context, it, onTextExtracted) } }
    }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { scope.launch { handleFileImport(context, it, onTextExtracted) } }
    }
    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) cameraImageUri.value?.let { scope.launch { extractTextFromImageAsync(context, it, onTextExtracted) } }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AssistChip(
            onClick = {
                cameraImageUri.value = createImageUri(context)
                cameraImageUri.value?.let { cameraLauncher.launch(it) }
            },
            label = { Text("Camera") },
            leadingIcon = { Icon(Icons.Default.CameraAlt, contentDescription = null) }
        )
        AssistChip(
            onClick = { imagePicker.launch("image/*") },
            label = { Text("Image") },
            leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) }
        )
        AssistChip(
            onClick = { filePicker.launch(arrayOf("application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")) },
            label = { Text("File") },
            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) }
        )
    }
}

// ------------------ Helper Functions ------------------

fun createImageUri(context: Context): Uri {
    val file = File(context.cacheDir, "camera_capture_${System.currentTimeMillis()}.jpg")
    return androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

fun saveSummaryHistory(inputText: String, summaryText: String) {
    val user = FirebaseAuth.getInstance().currentUser ?: return
    val firestore = FirebaseFirestore.getInstance()
    val data = hashMapOf(
        "uid" to user.uid,
        "type" to "summary",
        "inputText" to inputText,
        "resultText" to summaryText,
        "timestamp" to System.currentTimeMillis()
    )
    firestore.collection("study_history").add(data)
}

suspend fun handleFileImport(context: Context, uri: Uri, onResult: (String) -> Unit) {
    withContext(Dispatchers.IO) {
        val mime = context.contentResolver.getType(uri) ?: ""
        val text = when {
            mime.contains("pdf") -> PdfTextExtractor.extractText(context, uri)
            mime.contains("officedocument.wordprocessingml") || uri.toString().endsWith(".docx") -> {
                val file = uriToFile(context, uri)
                DocxTextExtractor.extractText(file)
            }
            else -> ""
        }
        withContext(Dispatchers.Main) {
            if (text.isEmpty()) Toast.makeText(context, "Unsupported file type", Toast.LENGTH_SHORT).show()
            onResult(text)
        }
    }
}

suspend fun extractTextFromImageAsync(context: Context, uri: Uri, onResult: (String) -> Unit) {
    withContext(Dispatchers.IO) {
        val bitmap = uriToBitmap(context, uri) ?: run { withContext(Dispatchers.Main) { onResult("") }; return@withContext }
        val recognizer = com.google.mlkit.vision.text.TextRecognition.getClient(
            com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS
        )
        var resultText = ""
        val latch = java.util.concurrent.CountDownLatch(1)
        val image = com.google.mlkit.vision.common.InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { resultText = it.text; latch.countDown() }
            .addOnFailureListener { latch.countDown() }
        latch.await()
        withContext(Dispatchers.Main) { onResult(resultText) }
    }
}

fun uriToFile(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)!!
    val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}")
    inputStream.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
    return file
}

suspend fun sendSummaryRequestAsync(
    input: String,
    language: String,
    length: String,
    format: String
): String = withContext(Dispatchers.IO) {
    try {
        // --------------------------
        // Step 1: Map length to concrete instructions
        // --------------------------
        val lengthInstruction = when(length.lowercase()) {
            "short" -> if(format.lowercase() == "bullet") "3-4 points" else "2-3 sentences"
            "medium" -> if(format.lowercase() == "bullet") "5-7 points" else "5-7 sentences"
            "long" -> if(format.lowercase() == "bullet") "8-10 points" else "8-12 sentences"
            else -> if(format.lowercase() == "bullet") "5-7 points" else "5-7 sentences"
        }

        // --------------------------
        // Step 2: Create system prompt based on format and language
        // --------------------------
        val systemPrompt = when(format.lowercase()) {
            "bullet" -> if (language.lowercase() == "filipino") {
                "Ikaw ay isang AI study assistant. Buodin ang ibinigay na teksto bilang **bullet points lamang**, $lengthInstruction. Huwag maglagay ng anumang panimulang teksto o markdown."
            } else {
                "You are an AI study assistant. Summarize the given text as **bullet points only**, $lengthInstruction. Do NOT include any introductory text or markdown."
            }
            else -> if (language.lowercase() == "filipino") {
                "Ikaw ay isang AI study assistant. Buodin ang ibinigay na teksto sa malinaw at maikling paraan bilang **isang talata**, $lengthInstruction."
            } else {
                "You are an AI study assistant. Summarize the given text clearly and concisely as **a paragraph**, $lengthInstruction."
            }
        }

        // --------------------------
        // Step 3: Create user prompt
        // --------------------------
        val userPrompt = when(format.lowercase()) {
            "bullet" -> if (language.lowercase() == "filipino") {
                "Buodin ang tekstong ito sa bullet points lamang:\n$input"
            } else {
                "Summarize this text in bullet points only:\n$input"
            }
            else -> if (language.lowercase() == "filipino") {
                "Buodin ang tekstong ito sa isang talata:\n$input"
            } else {
                "Summarize this text in a paragraph:\n$input"
            }
        }

        // --------------------------
        // Step 4: Prepare request to Cohere API
        // --------------------------
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

        // --------------------------
        // Step 5: Send request and handle response
        // --------------------------
        val response = CohereClient.api.chat("Bearer ${BuildConfig.COHERE_API_KEY}", request).awaitResponse()
        if (response.isSuccessful) {
            val chatResponse = response.body()
            val textBlocks = chatResponse?.message?.content ?: emptyList()
            // Clean up any leftover markdown (**)
            textBlocks.joinToString("\n") { it.text.replace("**", "").trim() }
        } else {
            "Failed to generate summary: ${response.code()} ${response.message()}"
        }

    } catch (e: Exception) {
        "Error generating summary: ${e.message}"
    }
}


