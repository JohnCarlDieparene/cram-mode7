package com.labactivity.crammode

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.labactivity.crammode.model.StudyHistory
import com.labactivity.crammode.utils.FlashcardUtils
import java.util.Calendar
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberDismissState
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.rememberDismissState
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.SnackbarDuration
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.material.Card
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.labactivity.crammode.R
import androidx.compose.foundation.border
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.shadow

class HomeComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                HomeScreenWithTabs()
            }
        }
    }
}

// ---------------------- Tabs ----------------------

@Composable
fun HomeScreenWithTabs() {
    var selectedTab by remember { mutableStateOf("home") }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == "home",
                    onClick = { selectedTab = "home" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == "history",
                    onClick = { selectedTab = "history" },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") }
                )
                NavigationBarItem(
                    selected = selectedTab == "profile",
                    onClick = { selectedTab = "profile" },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                "home" -> HomeContent()
                "history" -> StudyHistoryContent()
                "profile" -> ProfileContent()
            }
        }
    }
}

// ---------------------- Home ----------------------



@Composable
fun HomeContent() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ------------------- LOGO + APP TITLE -------------------
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp)
        ) {
            // Logo with border
            Box(
                modifier = Modifier
                    .size(140.dp) // size of the logo + border
                    .border(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(24.dp) // same as before
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.lojo),
                    contentDescription = "Cram Mode Logo",
                    modifier = Modifier
                        .size(140.dp), // actual logo size
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // App title
            Text(
                text = "Cram Mode",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // ------------------- STUDY MODE CARDS -------------------
        StudyModeCard(
            title = "Summary",
            description = "Condense notes instantly",
            icon = Icons.Default.Article,
            color = Color(0xFF4CAF50), // Green
            onClick = { context.startActivity(Intent(context, SummaryActivity::class.java)) }
        )
        StudyModeCard(
            title = "Flashcards",
            description = "Memorize key concepts",
            icon = Icons.Default.Style,
            color = Color(0xFF03A9F4), // Light Blue
            onClick = { context.startActivity(Intent(context, FlashcardsActivity::class.java)) }
        )
        StudyModeCard(
            title = "Quiz",
            description = "Test your knowledge",
            icon = Icons.Default.Quiz,
            color = Color(0xFF7E57C2), // Violet
            onClick = { context.startActivity(Intent(context, QuizActivity::class.java)) }
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}










@Composable
fun StudyModeCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontSize = 22.sp, color = Color.White)
                Text(description, fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))
            }
        }
    }
}

// ---------------------- History ----------------------

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StudyHistoryContent() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    val scope = rememberCoroutineScope()


    var historyList by remember { mutableStateOf<List<StudyHistory>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Filters
    var selectedType by remember { mutableStateOf("All") }
    var selectedDate by remember { mutableStateOf("All") }
    var dateExpanded by remember { mutableStateOf(false) }



    // Load history
    LaunchedEffect(Unit) {
        auth.currentUser?.let { user ->
            firestore.collection("study_history")
                .whereEqualTo("uid", user.uid)
                .get()
                .addOnSuccessListener { result ->
                    historyList = result.documents.mapNotNull { doc ->
                        doc.toObject(StudyHistory::class.java)?.apply { id = doc.id }
                    }.sortedByDescending { it.timestamp }
                    isLoading = false
                }
                .addOnFailureListener { isLoading = false }
        } ?: run { isLoading = false }
    }

    // Apply filters
    val filteredHistory = historyList.filter { item ->
        val typeMatch = selectedType == "All" || item.type.equals(selectedType, ignoreCase = true)
        val now = System.currentTimeMillis()
        val dateMatch = when (selectedDate) {
            "Today" -> isSameDay(item.timestamp, now)
            "This Week" -> isSameWeek(item.timestamp, now)
            "This Month" -> isSameMonth(item.timestamp, now)
            else -> true
        }
        typeMatch && dateMatch
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F7F7))) {

        FilterBar(
            selectedType = selectedType,
            onTypeChange = { selectedType = it },
            selectedDate = selectedDate,
            expanded = dateExpanded,
            onExpandChange = { dateExpanded = it },
            onDateChange = {
                selectedDate = it
                dateExpanded = false
            }
        )

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (historyList.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No history found", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredHistory.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No results for this filter", color = Color.Gray)
                        }
                    }
                } else {
                    items(filteredHistory, key = { it.id }) { item ->
                        var visible by remember { mutableStateOf(true) }

                        AnimatedVisibility(
                            visible = visible,
                            exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
                        ) {
                            HistoryCard(
                                item = item,
                                context = context,
                                onDelete = {
                                    // Animate out
                                    visible = false
                                    scope.launch {
                                        delay(300) // Wait for animation to finish
                                        historyList = historyList.filter { h -> h.id != item.id }
                                        firestore.collection("study_history").document(item.id).delete()
                                    }
                                }


                            )
                        }
                    }
                }
            }
        }


    }
}










@Composable
fun HistoryCard(item: StudyHistory, context: android.content.Context, onDelete: () -> Unit) {
    val titleType = item.type.replaceFirstChar { it.uppercase() }

    val previewText = when (item.type) {
        "flashcards" -> item.resultFlashcards.firstOrNull()?.question?.take(30) ?: ""
        "quiz" -> item.quiz.firstOrNull()?.question?.take(30) ?: ""
        "summary" -> item.resultText.take(30)
        else -> item.inputText.take(30)
    }

    val timestamp = java.text.SimpleDateFormat(
        "MMM dd, yyyy HH:mm",
        java.util.Locale.getDefault()
    ).format(java.util.Date(item.timestamp))

    val typeColor = when (item.type) {
        "quiz" -> Color(0xFF6200EE)
        "flashcards" -> Color(0xFF03A9F4)
        "summary" -> Color(0xFF4CAF50)
        else -> Color.Gray
    }

    val previewColor = when (item.type) {
        "quiz" -> Color(0xFF7E57C2)
        "flashcards" -> Color(0xFF29B6F6)
        "summary" -> Color(0xFF66BB6A)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                when (item.type) {
                    "summary" -> androidx.appcompat.app.AlertDialog.Builder(context)
                        .setTitle("Summary")
                        .setMessage(item.resultText)
                        .setPositiveButton("Close", null)
                        .show()
                    "flashcards" -> context.startActivity(
                        Intent(context, FlashcardViewerActivity::class.java).apply {
                            putParcelableArrayListExtra(
                                "flashcards",
                                ArrayList(item.resultFlashcards)
                            )
                        }
                    )
                    "quiz" -> context.startActivity(
                        Intent(context, QuizHistoryViewerActivity::class.java).apply {
                            putParcelableArrayListExtra(
                                "quiz_list",
                                ArrayList(item.quiz)
                            )
                            putExtra("readOnly", true)
                        }
                    )
                }
            },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                    .background(typeColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(titleType.first().toString(), fontWeight = FontWeight.Bold, color = typeColor)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$titleType – $previewText",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = previewColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (item.type) {
                        "quiz" -> "Score: ${item.quiz.count { it.userAnswer == it.correctAnswer }} / ${item.quiz.size}"
                        "flashcards" -> "Flashcards: ${item.resultFlashcards.size} cards"
                        else -> item.resultText.take(50)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = timestamp, style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
            }

            // 3-dot menu
            var menuExpanded by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(text = { Text("Delete") }, onClick = {
                        menuExpanded = false
                        onDelete()
                    })
                }
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterBar(
    selectedType: String,
    onTypeChange: (String) -> Unit,
    selectedDate: String,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onDateChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {

        // 🔹 Type filter row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Quiz", "Flashcards", "Summary").forEach { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { onTypeChange(type) },
                    label = { Text(type) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 🔹 Date filter (compact)
        Box {
            OutlinedButton(
                onClick = { onExpandChange(true) },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(selectedDate)
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandChange(false) }
            ) {
                listOf("All", "Today", "This Week", "This Month").forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            onDateChange(it)
                            onExpandChange(false)
                        }
                    )
                }
            }
        }
    }
}





fun isSameDay(ts: Long, now: Long): Boolean {
    val a = Calendar.getInstance().apply { timeInMillis = ts }
    val b = Calendar.getInstance().apply { timeInMillis = now }
    return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
}

fun isSameWeek(ts: Long, now: Long): Boolean {
    val a = Calendar.getInstance().apply { timeInMillis = ts }
    val b = Calendar.getInstance().apply { timeInMillis = now }
    return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.WEEK_OF_YEAR) == b.get(Calendar.WEEK_OF_YEAR)
}

fun isSameMonth(ts: Long, now: Long): Boolean {
    val a = Calendar.getInstance().apply { timeInMillis = ts }
    val b = Calendar.getInstance().apply { timeInMillis = now }
    return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.MONTH) == b.get(Calendar.MONTH)
}






// ---------------------- Profile ----------------------

data class ProfileStats(
    val summaries: Int = 0,
    val flashcards: Int = 0,
    val quizzes: Int = 0,
    val lastQuizScore: String = "—"
)

@Composable
fun ProfileContent() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var stats by remember { mutableStateOf(ProfileStats()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }

    val editProfileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        auth.currentUser?.reload()?.addOnCompleteListener {
            currentUser = auth.currentUser
        }
    }

    fun loadStats() {
        val uid = currentUser?.uid ?: return
        isLoading = true

        firestore.collection("study_history")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { result ->
                var summaries = 0
                var flashcards = 0
                var quizzes = 0
                var lastQuizScore = "—"

                // ✅ Get latest quiz
                val quizDocs = result.documents
                    .filter { it.getString("type") == "quiz" }
                    .sortedByDescending { it.getLong("timestamp") ?: 0L }

                if (quizDocs.isNotEmpty()) {
                    val latestQuiz = quizDocs.first()
                        .toObject(StudyHistory::class.java)
                        ?.quiz ?: emptyList()

                    if (latestQuiz.isNotEmpty()) {
                        val correct = latestQuiz.count {
                            it.userAnswer == it.correctAnswer
                        }
                        lastQuizScore = "$correct / ${latestQuiz.size}"
                    }
                }

                // ✅ Count study types
                for (doc in result.documents) {
                    when (doc.getString("type")) {
                        "summary" -> summaries++
                        "flashcards" -> flashcards++
                        "quiz" -> quizzes++
                    }
                }

                stats = ProfileStats(
                    summaries = summaries,
                    flashcards = flashcards,
                    quizzes = quizzes,
                    lastQuizScore = lastQuizScore
                )

                isLoading = false
                isRefreshing = false
            }
            .addOnFailureListener {
                isLoading = false
                isRefreshing = false
            }
    }

    LaunchedEffect(currentUser?.uid) {
        loadStats()
    }

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = {
            isRefreshing = true
            currentUser?.reload()?.addOnCompleteListener {
                currentUser = auth.currentUser
                loadStats()
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ---------------- PROFILE CARD ----------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (currentUser?.photoUrl != null) {
                                AsyncImage(
                                    model = currentUser?.photoUrl,
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(50))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            RoundedCornerShape(50)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentUser?.displayName
                                            ?.firstOrNull()?.uppercase()
                                            ?: currentUser?.email
                                                ?.firstOrNull()?.uppercase() ?: "?",
                                        color = Color.White,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Hello,", style = MaterialTheme.typography.labelMedium)
                            Text(
                                currentUser?.displayName
                                    ?: currentUser?.email
                                    ?: "Unknown User",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                editProfileLauncher.launch(
                                    Intent(context, EditProfileActivity::class.java)
                                )
                            },
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Edit")
                        }
                    }
                }
            }

            // ---------------- STATS ----------------
            Card(shape = RoundedCornerShape(20.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        ProfileStat("Summaries", stats.summaries.toString())
                        ProfileStat("Flashcards", stats.flashcards.toString())
                        ProfileStat("Quizzes", stats.quizzes.toString())
                    }
                }
            }

            // ---------------- LAST QUIZ SCORE ----------------

            Card(shape = RoundedCornerShape(20.dp)) {
                Column {
                    ProfileAction(Icons.Default.Settings, "Settings") {
                        context.startActivity(
                            Intent(context, SettingsActivity::class.java)
                        )
                    }
                    Divider()

                    ProfileAction(Icons.Default.Info, "About Cram Mode") {
                        context.startActivity(
                            Intent(context, AboutActivity::class.java)
                        )
                    }
                    Divider()

                    ProfileAction(Icons.Default.Description, "Privacy Policy") {
                        context.startActivity(
                            Intent(context, PrivacyPolicyActivity::class.java)
                        )
                    }
                    Divider()

                    ProfileAction(Icons.Default.Logout, "Logout", Color.Red) {
                        auth.signOut()
                        context.startActivity(
                            Intent(context, AuthComposeActivity::class.java)
                        )
                    }
                }
            }
        }
    }
}




// ---------------------- Helpers ----------------------

@Composable
fun RecentActivityItem(title: String, subtitle: String, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ProfileStat(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ProfileAction(icon: ImageVector, title: String, textColor: Color = MaterialTheme.colorScheme.onSurface, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = textColor)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = textColor, fontSize = 16.sp)
    }
}
