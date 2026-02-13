package com.labactivity.crammode

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

// -------------------- ACTIVITY --------------------
class FlashcardViewerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val flashcards = intent.getParcelableArrayListExtra<Flashcard>("flashcards") ?: arrayListOf()
        val title = intent.getStringExtra("title") ?: "Flashcards"

        setContent {
            MaterialTheme {
                val vm: FlashcardViewerVM = viewModel()
                vm.flashcards = flashcards

                FlashcardViewerScreen(
                    vm = vm,
                    title = title,
                    onBack = { finish() } // go back to previous activity
                )
            }
        }
    }
}

// -------------------- SCREEN --------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardViewerScreen(
    vm: FlashcardViewerVM,
    title: String,
    onBack: () -> Unit
) {
    // Track total drag for swipe
    var totalDrag by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0))
    ) {
        // ---------------- TopAppBar ----------------
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

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ---------------- Progress Indicator ----------------
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${vm.index + 1} / ${vm.flashcards.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                LinearProgressIndicator(
                    progress = if (vm.flashcards.isEmpty()) 0f else (vm.index + 1) / vm.flashcards.size.toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .padding(top = 8.dp),
                    color = Color(0xFF4CAF50),
                    trackColor = Color(0xFFE0E0E0)
                )
            }

            // ---------------- Flashcard ----------------
            if (vm.flashcards.isNotEmpty()) {
                val rotation by animateFloatAsState(
                    targetValue = if (vm.isFlipped) 180f else 0f,
                    animationSpec = tween(500)
                )
                val shadowElevation by animateFloatAsState(
                    targetValue = if (vm.isFlipped) 12f else 8f,
                    animationSpec = tween(500)
                )

                val cardBrush = if (!vm.isFlipped) {
                    Brush.verticalGradient(listOf(Color.White, Color(0xFFF5F5F5)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFFBBDEFB), Color(0xFF90CAF9)))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                        .shadow(shadowElevation.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardBrush)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { change, dragAmount ->
                                    totalDrag += dragAmount
                                },
                                onDragEnd = {
                                    if (totalDrag < -150f) vm.next()    // swipe left
                                    else if (totalDrag > 150f) vm.prev() // swipe right
                                    totalDrag = 0f
                                },
                                onDragCancel = {
                                    totalDrag = 0f
                                }
                            )
                        }

                        .clickable { vm.flip() }
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 12f * density
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (rotation <= 90f) {
                        Text(
                            text = vm.flashcards[vm.index].question,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                    } else {
                        Text(
                            text = vm.flashcards[vm.index].answer,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(24.dp)
                                .graphicsLayer { rotationY = 180f }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ---------------- Navigation Buttons ----------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { vm.prev() },
                        enabled = vm.index > 0,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Prev")
                        Spacer(Modifier.width(8.dp))
                        Text("Prev")
                    }

                    Button(
                        onClick = { vm.next() },
                        enabled = vm.index < vm.flashcards.lastIndex,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Next")
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tap the card to flip and see the answer",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}


// -------------------- VIEWMODEL --------------------
class FlashcardViewerVM : ViewModel() {
    var flashcards by mutableStateOf<List<Flashcard>>(emptyList())
    var index by mutableStateOf(0)
    var isFlipped by mutableStateOf(false)

    fun next() {
        if (index < flashcards.lastIndex) { index++; isFlipped = false }
    }

    fun prev() {
        if (index > 0) { index--; isFlipped = false }
    }

    fun flip() {
        isFlipped = !isFlipped
    }
}
