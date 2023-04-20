package com.example.chatgpt.api

import com.example.chatgpt.api.model.ChatCompletionRequest
import com.example.chatgpt.api.model.ChatCompletionResponse
import com.example.chatgpt.api.model.TranscriptResponse
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.util.concurrent.TimeUnit

interface OpenAiAPI {

    @Multipart
    @POST("audio/transcriptions")
    fun sendAudio(
        @Part audio: MultipartBody.Part,
        @Part("model") model: RequestBody,
    ): Call<TranscriptResponse>

    @POST("chat/completions")
    fun chatCompletion(
        @Body chatCompletionRequest: ChatCompletionRequest
    ): Call<ChatCompletionResponse>

    companion object {
        operator fun invoke(apiKey: String): OpenAiAPI {
            val gson = GsonBuilder()
                .setLenient()
                .create()

            val authInterceptor = Interceptor { chain ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $apiKey")
                    .build()
                chain.proceed(newRequest)
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl("https://api.openai.com/v1/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build()
                .create(OpenAiAPI::class.java)
        }
    }
}
