package com.labactivity.crammode

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.labactivity.crammode.model.QuizQuestion
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.geometry.Offset
import androidx.compose.animation.animateColorAsState

// -------------------- ACTIVITY --------------------
class QuizViewerActivity : ComponentActivity() {

    private val QuizLightColors = lightColorScheme(
        primary = Color(0xFF6200EE),
        onPrimary = Color.White,
        secondary = Color(0xFF03A9F4),
        onSecondary = Color.White,
        background = Color(0xFFF7F7F7),
        onBackground = Color.Black,
        surface = Color.White,
        onSurface = Color.Black,
        error = Color(0xFFF44336),
        onError = Color.White
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inputText = intent.getStringExtra("inputText") ?: ""
        val quizList: List<QuizQuestion>? = intent.getParcelableArrayListExtra("quiz_list")
        val readOnly = intent.getBooleanExtra("readOnly", false)
        val timePerQuestion = intent.getLongExtra("timePerQuestion", 15000L)

        if (quizList.isNullOrEmpty()) {
            finish()
            return
        }

        setContent {
            MaterialTheme(
                colorScheme = QuizLightColors,

            ) {
                QuizViewerScreen(
                    quizList = quizList,
                    readOnly = readOnly,
                    questionTimeMillis = timePerQuestion,
                    inputText = inputText,
                    title = if (readOnly) "Quiz History" else "Quiz",
                    onBack = { finish() }
                )
            }
        }
    }
}

// -------------------- SCREEN --------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizViewerScreen(
    quizList: List<QuizQuestion>,
    readOnly: Boolean,
    questionTimeMillis: Long,
    inputText: String,
    title: String = "Quiz",
    onBack: () -> Unit = {}
) {
    var resetKey by remember { mutableStateOf(0) }

    val shuffledQuizList by remember(resetKey) {
        mutableStateOf(quizList.shuffled())
    }

    val totalScore by remember(shuffledQuizList) {
        derivedStateOf {
            shuffledQuizList.count { it.userAnswer == it.correctAnswer }
        }
    }

    var currentIndex by remember(resetKey) { mutableStateOf(0) }
    var showScoreScreen by remember(resetKey) { mutableStateOf(false) }

    var selectedAnswer by remember(resetKey) { mutableStateOf<String?>(null) }
    var isAnswered by remember(resetKey) { mutableStateOf(false) }

    var timeLeft by remember(resetKey) { mutableStateOf(questionTimeMillis / 1000) }
    var timerKey by remember(resetKey) { mutableStateOf(0) }

    val currentQuestion = shuffledQuizList.getOrNull(currentIndex)

    // Pre-fill answers if readOnly
    LaunchedEffect(currentIndex, readOnly) {
        currentQuestion?.let { q ->
            if (readOnly) {
                selectedAnswer = q.userAnswer
                isAnswered = true
            }
        }
    }

    // Timer for non-readOnly
    LaunchedEffect(timerKey) {
        if (!readOnly && currentQuestion != null) {
            timeLeft = questionTimeMillis / 1000
            while (timeLeft > 0 && !isAnswered) {
                delay(1000)
                timeLeft--
            }
            if (timeLeft == 0L && !isAnswered) {
                currentQuestion.userAnswer = null
                isAnswered = true
                delay(1000)
                if (currentIndex < quizList.size - 1) {
                    currentIndex++
                    selectedAnswer = null
                    isAnswered = false
                    timerKey++
                } else {
                    saveQuizToHistory(quizList, inputText)
                    showScoreScreen = true
                }
            }
        }
    }

    if (showScoreScreen) {
        QuizScoreScreen(
            score = totalScore,
            total = quizList.size,
            onFinish = onBack,
            onRetry = {
                shuffledQuizList.forEach { it.userAnswer = null }
                resetKey++
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7)) // fixed background color
    ) {
        // ---------------- TopAppBar ----------------
        SmallTopAppBar(
            title = { Text(title, color = Color.Black) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                }
            },
            colors = TopAppBarDefaults.smallTopAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color.Black,
                navigationIconContentColor = Color.Black
            )
        )

        currentQuestion?.let { question ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ---------------- Question header + progress ----------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Q${currentIndex + 1}/${quizList.size}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        )
                        LinearProgressIndicator(
                            progress = ((currentIndex + 1) / quizList.size.toFloat()),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF6200EE),
                            trackColor = Color(0xFFE0E0E0)
                        )
                    }

                    if (!readOnly) {
                        val timerFraction by animateFloatAsState(
                            targetValue = timeLeft.toFloat() / (questionTimeMillis / 1000),
                            animationSpec = tween(1000)
                        )
                        val timerColor = when {
                            timerFraction > 0.6f -> Color(0xFF4CAF50)
                            timerFraction > 0.3f -> Color(0xFFFFC107)
                            else -> Color(0xFFF44336)
                        }

                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = timerFraction,
                                strokeWidth = 6.dp,
                                color = timerColor,
                                modifier = Modifier.size(60.dp)
                            )
                            Text(
                                text = "${timeLeft}s",
                                fontWeight = FontWeight.Bold,
                                color = timerColor
                            )
                        }
                    }
                }

                // ---------------- Question Card ----------------
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        text = question.question,
                        modifier = Modifier.padding(24.dp),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                }

                // ---------------- Options ----------------
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    question.options.forEach { option ->
                        val targetColor = when {
                            isAnswered && option == question.correctAnswer -> Color(0xFF4CAF50)
                            isAnswered && option == selectedAnswer && selectedAnswer != question.correctAnswer -> Color(0xFFF44336)
                            !isAnswered && option == selectedAnswer -> Color(0xFFBBDEFB)
                            else -> Color.White
                        }

                        val animatedBgColor by animateColorAsState(
                            targetValue = targetColor,
                            animationSpec = tween(durationMillis = 180, easing = LinearOutSlowInEasing)
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp)
                                .clickable(enabled = !isAnswered && !readOnly) {
                                    selectedAnswer = option
                                },
                            colors = CardDefaults.cardColors(containerColor = animatedBgColor),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.CenterStart,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = option,
                                    color = if (animatedBgColor == Color.White) Color.Black else Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // ---------------- Feedback ----------------
                AnimatedVisibility(
                    visible = isAnswered || readOnly,
                    enter = fadeIn(tween(120)) + scaleIn(initialScale = 0.92f, animationSpec = tween(120)),
                    exit = fadeOut(tween(80))
                ) {
                    Text(
                        text = when {
                            selectedAnswer == question.correctAnswer -> "✅ Correct!"
                            selectedAnswer == null -> "⏰ Time's up! Correct: ${question.correctAnswer}"
                            else -> "❌ Incorrect! Correct: ${question.correctAnswer}"
                        },
                        color = when {
                            selectedAnswer == question.correctAnswer -> Color(0xFF4CAF50)
                            selectedAnswer == null -> Color.Gray
                            else -> Color(0xFFF44336)
                        },
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ---------------- Submit / Next / Finish ----------------
                Button(
                    onClick = {
                        if (!isAnswered) {
                            isAnswered = true
                            question.userAnswer = selectedAnswer
                        } else {
                            if (currentIndex < quizList.size - 1) {
                                currentIndex++
                                selectedAnswer = null
                                isAnswered = false
                                timerKey++
                            } else {
                                if (!readOnly) saveQuizToHistory(quizList, inputText)
                                showScoreScreen = true
                            }
                        }
                    },
                    enabled = if (!isAnswered) selectedAnswer != null else true,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (!isAnswered) "Submit"
                        else if (currentIndex < quizList.size - 1) "Next"
                        else "Finish"
                    )
                }
            }
        }
    }
}

// -------------------- Score Screen --------------------
@Composable
fun QuizScoreScreen(
    score: Int,
    total: Int,
    onFinish: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
    val percentage = score.toFloat() / total
    val showConfetti = percentage >= 0.7f
    val isPerfect = score == total

    var confettiList by remember(showConfetti) {
        mutableStateOf(if (showConfetti) generateConfetti() else emptyList())
    }

    LaunchedEffect(showConfetti) {
        if (!showConfetti) return@LaunchedEffect
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < 3000) {
            confettiList = confettiList.map { it.copy(y = it.y + it.speed) }
            delay(16)
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isPerfect) 1.1f else 1f,
        animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse)
    )

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F7F7))) {
        if (showConfetti) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                confettiList.forEach { c -> drawCircle(c.color, c.size, Offset(c.x, c.y)) }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isPerfect) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD700))
                        .graphicsLayer(scaleX = scale, scaleY = scale),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏆", fontSize = 36.sp)
                }
                Spacer(Modifier.height(16.dp))
            }

            Text(
                text = when {
                    isPerfect -> "Outstanding!"
                    percentage >= 0.7f -> "Great job!"
                    else -> "Keep practicing!"
                },
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontSize = 24.sp
            )

            Spacer(Modifier.height(16.dp))
            Text("Your Score", color = Color.Black, fontSize = 20.sp)
            Text(
                "$score / $total",
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                color = when {
                    isPerfect -> Color(0xFF4CAF50)
                    percentage >= 0.7f -> Color(0xFF6200EE)
                    else -> Color.Gray
                }
            )
            Text("${(percentage * 100).toInt()}%", color = Color.Black, fontSize = 20.sp)

            Spacer(Modifier.height(32.dp))

            if (onRetry != null) {
                OutlinedButton(onClick = onRetry, modifier = Modifier.fillMaxWidth()) { Text("Retry Quiz") }
                Spacer(Modifier.height(12.dp))
            }

            Button(onClick = onFinish, modifier = Modifier.fillMaxWidth()) { Text("Back to Home") }
        }
    }
}

// -------------------- Confetti --------------------
data class Confetti(val x: Float, val y: Float, val size: Float, val color: Color, val speed: Float)
fun generateConfetti(): List<Confetti> {
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Magenta, Color.Cyan)
    return List(100) {
        Confetti(
            x = Random.nextFloat() * 1080f,
            y = Random.nextFloat() * 1920f,
            size = Random.nextFloat() * 12 + 4,
            color = colors.random(),
            speed = Random.nextFloat() * 8 + 2
        )
    }
}

// -------------------- Firebase --------------------
private fun saveQuizToHistory(quizList: List<QuizQuestion>, inputText: String) {
    val user = FirebaseAuth.getInstance().currentUser ?: return
    val firestore = FirebaseFirestore.getInstance()

    val history = hashMapOf(
        "uid" to user.uid,
        "type" to "quiz",
        "inputText" to inputText,
        "quiz" to quizList,
        "timestamp" to System.currentTimeMillis()
    )

    firestore.collection("study_history")
        .add(history)
        .addOnSuccessListener { Log.d("QuizHistory", "Quiz saved") }
        .addOnFailureListener { Log.e("QuizHistory", "Save failed", it) }
}
