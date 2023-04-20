package com.example.chatgpt.api.model
import com.google.gson.annotations.SerializedName

data class ChatCompletionResponse(
    val id: String,
    @SerializedName("object")
    val responseObject: String,
    val created: Int,
    val model: String,
    val usage: Usage,
    val choices: List<Choice>
)