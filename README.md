OPENAI API:
    Este es un archivo de interfaz que define una API de OpenAI para interactuar con su servicio de chat
    y transcripción de audio. Está construido utilizando la biblioteca Retrofit, que es una biblioteca
    de cliente HTTP para Android y Java que simplifica el consumo de servicios web.

    La interfaz define dos métodos: "sendAudio" y "chatCompletion". El método "sendAudio" se utiliza
    para enviar archivos de audio para la transcripción, mientras que el método "chatCompletion" se
    utiliza para obtener una respuesta de conversación de OpenAI basada en un mensaje de chat dado.
    
    La sección "companion object" contiene un método "invoke" que se utiliza para inicializar la API con
    una clave de API de OpenAI. En este método, se define un interceptor de autenticación que agrega el
    encabezado de autenticación a cada solicitud realizada a la API.
    
    También se define un objeto de cliente de OkHttp que se utiliza para realizar solicitudes HTTP y
    configurar tiempos de espera para las solicitudes. Finalmente, se crea y devuelve un objeto de la
    interfaz OpenAiAPI utilizando la instancia Retrofit construida.

MAIN ACTIVITY (Kotlin):
    Este es un archivo de código fuente para una aplicación de chat que utiliza la API de OpenAI para
    proporcionar respuestas a los usuarios en función de los mensajes de chat ingresados o del audio
    grabado. La aplicación está construida utilizando Android Studio y el lenguaje de programación
    Kotlin.

    La actividad principal de la aplicación es "MainActivity", que es la pantalla principal que los
    usuarios ven cuando abren la aplicación. La actividad define una serie de variables y objetos que se
    utilizan en la aplicación, como el objeto "LoadingScreen", "TextToSpeech", y "MediaRecorder".
    
    El método "onCreate" inicializa y configura las variables y objetos necesarios para la actividad,
    incluida la verificación del permiso de grabación de audio y la configuración del objeto
    TextToSpeech. El método "initViews" inicializa la interfaz de usuario y configura los listeners de
    los botones de envío de chat y grabación de audio. El método "sendTextPrompt" se utiliza para enviar
    mensajes de chat a la API de OpenAI para generar respuestas, y el método "sendAudio" se utiliza para
    enviar archivos de audio grabados a la API para transcripción.
    
    La aplicación también define una serie de métodos auxiliares para la interfaz de usuario, como "
    addMessage", que agrega mensajes de chat a la vista de chat, y "toggleRecordingIndicator", que
    muestra un indicador de grabación de audio en la pantalla mientras se graba.

ACTIVITY MAIN (xml)
    Este es el código XML para la vista de la actividad en Android. Es una estructura de diseño en capas
    que utiliza vistas como EditText, ImageView y ScrollView para crear una interfaz de usuario para la
    aplicación. La vista principal es un RelativeLayout que contiene todas las demás vistas.
    
    Hay un EditText para introducir el token de la API en la parte superior de la pantalla.
    Un LinearLayout que contiene un ScrollView y un ImageView para enviar la entrada de texto como una
    pregunta al modelo de IA y un ImageView para enviar audio. 
    Debajo de esto, hay otro LinearLayout que
    contiene las respuestas del modelo en un TextView. 
    En la parte inferior derecha, hay un ImageView
    para eliminar todo el contenido y un ImageView para mostrar una animación de grabación durante la
    grabación de audio.
