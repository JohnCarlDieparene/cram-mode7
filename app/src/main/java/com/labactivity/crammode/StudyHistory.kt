package com.labactivity.crammode.model

import com.labactivity.crammode.Flashcard

data class StudyHistory(
    var id: String = "",
    val uid: String = "",
    val type: String = "",          // "flashcards", "summary", or "quiz"
    val inputText: String = "",
    val timestamp: Long = 0L,
    val resultText: String = "",    // used for summaries
    val resultFlashcards: List<Flashcard> = emptyList(), // <-- add this
    val quiz: List<QuizQuestion> = emptyList()           // existing
)
