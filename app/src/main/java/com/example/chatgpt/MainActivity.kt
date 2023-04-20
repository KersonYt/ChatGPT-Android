package com.example.chatgpt

import android.Manifest
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.example.chatgpt.api.*
import com.example.chatgpt.api.model.ChatCompletionRequest
import com.example.chatgpt.api.model.ChatCompletionResponse
import com.example.chatgpt.api.model.Message
import com.example.chatgpt.api.model.TranscriptResponse
import com.example.chatgpt.popup.LoadingScreen
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var loading: LoadingScreen
    private lateinit var tts: TextToSpeech
    private var isRecording = false
    private var mediaRecorder: MediaRecorder? = null
    private lateinit var audioFile: File

    private lateinit var messages: MutableList<Message>

    private lateinit var sendPrompt: ImageView
    private lateinit var sendAudio: ImageView
    private lateinit var deleteButton: ImageView
    private lateinit var inputPrompt: EditText
    private lateinit var tokenApi: EditText
    private lateinit var finalResultContainer: LinearLayout

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sendPrompt = findViewById(R.id.sendPropmt)
        sendAudio = findViewById(R.id.sendAudio)
        deleteButton = findViewById(R.id.deleteAll)
        inputPrompt = findViewById(R.id.inputPrompt)
        tokenApi = findViewById(R.id.apiToken)
        finalResultContainer = findViewById(R.id.finalResultContainer)

        val bot = Message("system", "Eres un asistente de voz")
        messages = mutableListOf(bot)

        checkRecordAudioPermission()
        initTextToSpeech()
        initViews()
    }

    private fun checkRecordAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 0)
        }
    }

    private fun initTextToSpeech() {
        tts = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale("es", "ES")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initViews() {
        configureInputPrompt(inputPrompt)
        setupSendPromptClickListener(sendPrompt, inputPrompt, tokenApi, finalResultContainer)
        setupSendAudioClickListener(sendAudio, tokenApi)
        setupDeleteButtonClickListener(deleteButton, inputPrompt, finalResultContainer)
    }

    private fun configureInputPrompt(inputPrompt: EditText) {
        inputPrompt.setHorizontallyScrolling(false)
    }

    private fun setupSendPromptClickListener(
        sendPrompt: ImageView,
        inputPrompt: EditText,
        tokenApi: EditText,
        finalResultContainer: LinearLayout
    ) {

        sendPrompt.setOnClickListener {
            if (inputPrompt.text?.isEmpty() == true) {
                showToastMessage("Introduce un prompt")
            } else if (tokenApi.text.isEmpty()) {
                showToastMessage("Introduce tu token para poder hacer la llamada")
            } else {
                sendTextPrompt(inputPrompt, tokenApi, finalResultContainer, messages)
            }
        }
    }

    private fun setupSendAudioClickListener(sendAudio: ImageView, tokenApi: EditText) {
        sendAudio.setOnClickListener {
            if (tokenApi.text.isEmpty()) {
                showToastMessage("Introduce tu token para poder hacer la llamada")
            } else {
                if (isRecording) {
                    stopRecordingAndSendAudio(tokenApi)
                } else {
                    startRecording()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setupDeleteButtonClickListener(
        deleteButton: ImageView,
        inputPrompt: EditText,
        finalResultContainer: LinearLayout
    ) {
        deleteButton.setOnClickListener {
            inputPrompt.text.clear()
            finalResultContainer.removeAllViews()
            messages.removeIf { message -> message.role != "system" }
        }
    }

    private fun showToastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun addMessage(container: LinearLayout, message: String, isQuestion: Boolean) {
        val paddingInDp = 20
        val paddingInPx = (paddingInDp * resources.displayMetrics.density).toInt()

        val textView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = if (isQuestion) Gravity.END else Gravity.START
            }
            text = HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_LEGACY)
            setTextColor(Color.WHITE)
            textSize = 17f
            if (isQuestion) {
                setPadding(paddingInPx, 0, 0, 0) // Añadir padding a la izquierda para preguntas
            } else {
                setPadding(0, 0, paddingInPx, 0) // Añadir padding a la derecha para respuestas
            }
        }
        container.addView(textView)
    }


    private fun sendTextPrompt(
        inputPrompt: EditText,
        tokenApi: EditText,
        finalResultContainer: LinearLayout,
        messages: MutableList<Message>
    ) {
        loading = LoadingScreen(this)
        loading.execute()

        val question = "<b>Pregunta:</b> ${inputPrompt.text}<br>"
        addMessage(finalResultContainer, question, true)

        val message = Message("user", inputPrompt.text.toString())
        messages.add(message)
        val request = ChatCompletionRequest("gpt-3.5-turbo", messages)
        OpenAiAPI(tokenApi.text.toString()).chatCompletion(request).enqueue(object : Callback<ChatCompletionResponse> {
            override fun onFailure(call: Call<ChatCompletionResponse>, t: Throwable) {
                showToastMessage("Error llamando al servicio de ChatCompletion")
                loading.isDismiss()
            }

            override fun onResponse(
                call: Call<ChatCompletionResponse>,
                response: Response<ChatCompletionResponse>
            ) {
                val chatGpt = "<b>ChatGPT:</b> ${response.body()?.choices?.get(0)?.message?.content}<br>"
                addMessage(finalResultContainer, chatGpt, false)

                val chatGptPlainText = response.body()?.choices?.get(0)?.message?.content ?: ""
                speakText(chatGptPlainText)

                loading.isDismiss()
            }
        })
    }

    private fun stopRecordingAndSendAudio(tokenApi: EditText) {
        toggleRecordingIndicator(false)
        loading = LoadingScreen(this)
        loading.execute()
        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null
        isRecording = false

        sendAudio(tokenApi)
    }

    private fun sendAudio(tokenApi: EditText) {
        val audioRequestBody = audioFile.asRequestBody("audio/m4a".toMediaTypeOrNull())
        val audioPart = MultipartBody.Part.createFormData("file", audioFile.name, audioRequestBody)
        val modelRequestBody = "whisper-1".toRequestBody("whisper-1".toMediaTypeOrNull())

        if (audioFile.length() == 0L) {
            showToastMessage("El archivo de audio está vacío.")
            loading.isDismiss()
        } else {
            OpenAiAPI(tokenApi.text.toString()).sendAudio(
                audio = audioPart,
                model = modelRequestBody
            ).enqueue(object : Callback<TranscriptResponse> {
                override fun onFailure(call: Call<TranscriptResponse>, t: Throwable) {
                    showToastMessage("Error al llamar al servicio de SpeechToText")
                    loading.isDismiss()
                }

                override fun onResponse(
                    call: Call<TranscriptResponse>,
                    response: Response<TranscriptResponse>
                ) {
                    if (!response.isSuccessful) {
                        val errorMessage = response.errorBody()?.string() ?: "Error desconocido"
                        showToastMessage(
                            "Error al llamar al servicio de SpeechToText. Código de error: ${response.code()}, Mensaje: $errorMessage"
                        )
                        loading.isDismiss()
                        return
                    }
                    val transcribedText = response.body()?.text ?: ""
                    inputPrompt.setText(transcribedText)
                    loading.isDismiss()
                }
            })
        }
    }

    private fun startRecording() {
        audioFile = File(externalCacheDir?.absolutePath + "/audio.m4a")

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(audioFile.absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        }

        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            toggleRecordingIndicator(true)
            isRecording = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun speakText(text: String) {
        val utteranceId = this.hashCode().toString() + System.currentTimeMillis()
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}

            override fun onDone(utteranceId: String?) {}

            override fun onError(utteranceId: String?) {}
        })

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    override fun onDestroy() {
        if (this::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

    private fun toggleRecordingIndicator(visible: Boolean) {
        val recordingIndicator: ImageView = findViewById(R.id.recording_indicator)
        if (visible) {
            val blinkAnimation = ObjectAnimator.ofInt(recordingIndicator, "alpha", 0, 255).apply {
                duration = 1000
                repeatMode = ObjectAnimator.REVERSE
                repeatCount = ObjectAnimator.INFINITE
            }
            recordingIndicator.visibility = View.VISIBLE
            blinkAnimation.start()
        } else {
            recordingIndicator.visibility = View.GONE
        }
    }
}


