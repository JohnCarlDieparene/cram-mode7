    // --- ChatModels.kt ---
    package com.labactivity.crammode

    // ✅ Request object for Cohere v2 Chat API
    data class ChatRequest(
        val model: String = "command-a-03-2025",    // Stable chat model
        val messages: List<ChatMessage>          // Conversation messages
    )

    // ✅ Each chat message must have a role and a list of content blocks
    data class ChatMessage(
        val role: String,                        // "system", "user", "assistant"
        val content: List<MessageContent>        // List of content blocks
    )

    // ✅ The content of a chat message
    data class MessageContent(
        val type: String = "text",               // Always "text" for now
        val text: String                         // Actual message text
    )

    // ✅ Response from Cohere v2 Chat API
    data class ChatResponse(
        val id: String,                          // Response ID
        val message: ChatMessageResponse         // The assistant's reply
    )

    // ✅ Assistant's reply message
    data class ChatMessageResponse(
        val role: String,                        // "assistant"
        val content: List<MessageContent>        // List of content blocks
    )
