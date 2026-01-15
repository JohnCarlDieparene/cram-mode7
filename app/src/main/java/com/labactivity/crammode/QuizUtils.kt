    package com.labactivity.crammode.utils

    import com.labactivity.crammode.model.QuizQuestion

    object QuizUtils {
        fun parseQuizQuestions(raw: String): List<QuizQuestion> {
            val questions = mutableListOf<QuizQuestion>()

            // Split into blocks (each Question…Answer group)
            val blocks = raw.trim().split(Regex("(?=Question:|Tanong:)", RegexOption.IGNORE_CASE))

            for (block in blocks) {
                val questionMatch = Regex("""(?:Question|Tanong):\s*(.+?)\n""", RegexOption.IGNORE_CASE).find(block)

                // Matches A-D (or a-d) with "." or ")" as separator
                val choicesMatch = Regex("""([A-Da-d])[.)]\s*(.+?)(?:\n|$)""").findAll(block)

                val answerMatch = Regex("""(?:Answer|Sagot):\s*([A-Da-d])""", RegexOption.IGNORE_CASE).find(block)

                if (questionMatch != null && answerMatch != null) {
                    val question = questionMatch.groupValues[1].trim()
                    val options = choicesMatch.map { it.groupValues[2].trim() }.toList()
                    val correctLetter = answerMatch.groupValues[1].uppercase()
                    val correctIndex = "ABCD".indexOf(correctLetter)
                    val correctAnswer = options.getOrNull(correctIndex).orEmpty()

                    // Only add if we have at least 2 options (not strictly 4) and a valid correct answer
                    if (question.isNotEmpty() && options.isNotEmpty() && correctAnswer.isNotEmpty()) {
                        questions.add(
                            QuizQuestion(
                                question = question,
                                options = options,
                                correctAnswer = correctAnswer,
                                userAnswer = null
                            )
                        )
                    }
                }
            }

            return questions
        }
    }

