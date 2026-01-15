package com.labactivity.crammode

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.labactivity.crammode.model.QuizQuestion
import kotlinx.coroutines.delay

class QuizHistoryViewerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val quizList: List<QuizQuestion>? =
            intent.getParcelableArrayListExtra("quiz_list")

        if (quizList.isNullOrEmpty()) finish()

        setContent {
            QuizHistoryViewerScreen(
                quizList = quizList!!,
                title = "Quiz History",
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizHistoryViewerScreen(
    quizList: List<QuizQuestion>,
    title: String = "Quiz History",
    onBack: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }
    val currentQuestion = quizList[currentIndex]
    val totalScore by remember { derivedStateOf { quizList.count { it.userAnswer == it.correctAnswer } } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7)) // light background
    ) {

        // Top AppBar
        SmallTopAppBar(
            title = { Text(title) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.smallTopAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color.Black,
                navigationIconContentColor = Color.Black
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Question header + progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Q${currentIndex + 1}/${quizList.size}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                LinearProgressIndicator(
                    progress = ((currentIndex + 1) / quizList.size.toFloat()),
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .padding(start = 16.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF90CAF9), // soft blue
                    trackColor = Color(0xFFE0E0E0)
                )
            }

            // Question card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4FF)) // light blue card
            ) {
                Text(
                    text = currentQuestion.question,
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                )
            }

            // Options (read-only)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                currentQuestion.options.forEach { option ->
                    val bgColor = when {
                        option == currentQuestion.correctAnswer -> Color(0xFFDFF2E1) // green-ish for correct
                        option == currentQuestion.userAnswer && currentQuestion.userAnswer != currentQuestion.correctAnswer -> Color(0xFFFDE2E2) // red-ish for wrong
                        else -> Color.White
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (bgColor == Color.White) Color.Black else Color.DarkGray
                            )
                        }
                    }
                }
            }

            // Score
            Text(
                text = "Score: $totalScore / ${quizList.size}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xFF333333),
                modifier = Modifier.align(Alignment.End)
            )

            // Navigation buttons (simplified)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { if (currentIndex > 0) currentIndex-- },
                    enabled = currentIndex > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Previous")
                }

                Spacer(Modifier.width(16.dp))

                Button(
                    onClick = { if (currentIndex < quizList.lastIndex) currentIndex++ },
                    enabled = currentIndex < quizList.lastIndex,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Next")
                }
            }
        }
    }
}
