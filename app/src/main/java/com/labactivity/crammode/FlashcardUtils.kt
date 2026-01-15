package com.labactivity.crammode.utils

import com.labactivity.crammode.Flashcard

object FlashcardUtils {

    fun parseFlashcards(raw: String): List<Flashcard> {
        val flashcards = mutableListOf<Flashcard>()

        // stricter regex: Q: at line start, A: at line start, capture until next Q: or end
        val pattern = Regex(
            """(?m)^Q:\s*(.+?)\r?\nA:\s*(.+?)(?=(\r?\nQ:)|\z)""",
            setOf(RegexOption.DOT_MATCHES_ALL)
        )

        pattern.findAll(raw).forEach { match ->
            val question = match.groupValues[1].trim()
            val answer = match.groupValues[2].trim()
            if (question.isNotEmpty() && answer.isNotEmpty()) {
                flashcards.add(Flashcard(question, answer))
            }
        }

        return flashcards
    }

}
