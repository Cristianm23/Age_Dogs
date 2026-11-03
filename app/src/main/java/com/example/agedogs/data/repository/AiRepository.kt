package com.example.agedogs.data.repository

import com.example.agedogs.data.model.AiRequest
import com.example.agedogs.data.model.MensajeIa
import com.example.agedogs.data.remote.AiApiService
import com.example.agedogs.data.remote.FirebaseProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class AiRepository(
    private val firestore: FirebaseFirestore = FirebaseProvider.firestore,
    private val apiService: AiApiService = AiApiService.create()
) {

    companion object {
        const val SISTEMA_PROMPT = """
            Eres un asistente de cuidado canino. Respondes en español, claro y breve.
            Temas: alimentación, higiene, cuidados generales, comportamiento básico.
            No diagnosticas, no recetas medicamentos ni das dosis.
            Marca urgencia=true ante señales como: dificultad para respirar, convulsiones, sangrado abundante, posible intoxicación (chocolate, xilitol, veneno, medicamentos humanos), abdomen hinchado con arcadas, atropello o golpe fuerte, imposibilidad de orinar, golpe de calor, encías pálidas o azuladas, pérdida de consciencia, parto con complicaciones, vómito o diarrea con sangre o persistentes.
            Si hay urgencia, lo primero que dices es que acudan de inmediato a un veterinario.
        """

        val EMERGENCY_KEYWORDS = listOf(
            "respirar", "convulsiones", "sangrado", "intoxicación", "chocolate", "xilitol",
            "veneno", "hinchado", "atropello", "orinar", "calor", "pálidas", "azuladas",
            "inconsciencia", "parto", "vómito", "diarrea", "sangre"
        )
    }

    suspend fun consultarIa(
        usuarioId: String,
        mensaje: String,
        perroContexto: String?,
        webhookUrl: String = "https://hook.eu1.make.com/your-webhook-endpoint"
    ): Result<MensajeIa> {
        return try {
            val request = AiRequest(
                mensaje = mensaje,
                perroContexto = perroContexto,
                sistemaPrompt = SISTEMA_PROMPT.trimIndent()
            )

            val respuestaTexto = when {
                webhookUrl.startsWith("sk-") -> {
                    // LLAMADA DIRECTA A OPENAI CHATGPT CON LA API KEY DEL USUARIO (sk-...)
                    llamarOpenAiDirecto(webhookUrl, mensaje, perroContexto)
                }
                webhookUrl.startsWith("http") -> {
                    // LLAMADA A WEBHOOK EXTERNO (Make / Zapier / Backend)
                    try {
                        val response = apiService.consultarIa(webhookUrl, request)
                        response.respuesta
                    } catch (_: Exception) {
                        generarRespuestaLocal(mensaje)
                    }
                }
                else -> {
                    // FALLBACK LOCAL AUTOMÁTICO
                    generarRespuestaLocal(mensaje)
                }
            }

            val lowerMensaje = mensaje.lowercase()
            val lowerRespuesta = respuestaTexto.lowercase()
            val hasKeyword = EMERGENCY_KEYWORDS.any { lowerMensaje.contains(it) || lowerRespuesta.contains(it) }

            val mensajeIa = MensajeIa(
                id = "",
                usuarioId = usuarioId,
                pregunta = mensaje,
                respuesta = respuestaTexto,
                urgencia = hasKeyword,
                motivo = if (hasKeyword) "Palabra clave de urgencia detectada" else null,
                fecha = System.currentTimeMillis()
            )

            val docRef = firestore.collection("usuarios").document(usuarioId)
                .collection("iaHistorial").document()
            
            val savedMensaje = mensajeIa.copy(id = docRef.id)
            docRef.set(savedMensaje).await()

            Result.success(savedMensaje)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun llamarOpenAiDirecto(apiKey: String, mensaje: String, perroContexto: String?): String {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val json = Json { ignoreUnknownKeys = true }

                val promptCompleto = if (!perroContexto.isNullOrBlank()) {
                    "$perroContexto. Consulta del usuario: $mensaje"
                } else {
                    mensaje
                }

                val systemPromptClean = SISTEMA_PROMPT.trimIndent().replace("\n", " ")
                // Actualizado a gpt-4o-mini (gpt-3.5-turbo fue deprecado por OpenAI)
                val jsonBody = """
                    {
                      "model": "gpt-4o-mini",
                      "messages": [
                        {"role": "system", "content": "$systemPromptClean"},
                        {"role": "user", "content": "$promptCompleto"}
                      ],
                      "temperature": 0.7
                    }
                """.trimIndent()

                val body = jsonBody.toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBodyStr = response.body?.string() ?: "{}"

                if (!response.isSuccessful) {
                    return@withContext "Error OpenAI (HTTP ${response.code}): $responseBodyStr"
                }

                val jsonElement = json.parseToJsonElement(responseBodyStr)
                val choices = jsonElement.jsonObject["choices"]?.jsonArray ?: return@withContext "Respuesta vacía de OpenAI"
                val firstChoice = choices[0].jsonObject
                val message = firstChoice["message"]?.jsonObject
                val content = message?.get("content")?.jsonPrimitive?.content ?: "Sin contenido"
                content.trim()
            } catch (e: Exception) {
                "Error al conectar con OpenAI: ${e.localizedMessage}"
            }
        }
    }

    private fun generarRespuestaLocal(pregunta: String): String {
        val p = pregunta.lowercase()
        return when {
            p.contains("comida") || p.contains("aliment") || p.contains("dar de comer") || p.contains("concentrado") ->
                "Los perros adultos suelen requerir de 1 a 2 comidas al día con un alimento balanceado adecuado a su peso y etapa de vida. Evita darle alimentos tóxicos como chocolate, uvas, cebolla o xilitol."
            p.contains("baño") || p.contains("bañar") || p.contains("higiene") || p.contains("pelo") ->
                "Se recomienda bañar a un perro cada 3 a 4 semanas, dependiendo de su tipo de pelaje y actividad. Usa siempre shampoo especializado para caninos para proteger su pH."
            p.contains("vacuna") || p.contains("vacunar") || p.contains("parásito") ->
                "El esquema de vacunación incluye la polivalente (parvovirus, moquillo, etc.) y la rabia. Consulta con tu médico veterinario de confianza para mantener su cartilla al día."
            p.contains("vomito") || p.contains("diarrea") || p.contains("sangre") || p.contains("respirar") || p.contains("convulsiones") || p.contains("atropello") ->
                "🚨 ¡ALERTA DE POSIBLE URGENCIA! Los síntomas o situación que mencionas requieren atención veterinaria inmediata. Por favor acude a la clínica más cercana de inmediato."
            else ->
                "Entiendo tu consulta sobre tu peludo. Como asistente canino virtual, te recomiendo mantenerlo hidratado y observar su comportamiento. Siri notas decaimiento o dolor persistente, consulta con tu médico veterinario."
        }
    }

    suspend fun obtenerHistorial(usuarioId: String): Result<List<MensajeIa>> {
        return try {
            val snapshot = firestore.collection("usuarios").document(usuarioId)
                .collection("iaHistorial")
                .orderBy("fecha", Query.Direction.ASCENDING)
                .get()
                .await()

            val historial = snapshot.documents.mapNotNull { it.toObject(MensajeIa::class.java) }
            Result.success(historial)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
