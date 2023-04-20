package com.example.chatgpt.api.model

data class ChatCompletionRequest (
    val model: String,
    val messages: List<Message>
)