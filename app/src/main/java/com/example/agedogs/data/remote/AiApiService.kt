package com.example.agedogs.data.remote

import com.example.agedogs.data.model.AiRequest
import com.example.agedogs.data.model.AiResponse
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface AiApiService {
    @POST
    suspend fun consultarIa(@Url url: String, @Body request: AiRequest): AiResponse

    companion object {
        private const val DEFAULT_WEBHOOK_URL = "https://hook.eu1.make.com/your-webhook-endpoint"

        fun create(baseUrl: String = "https://hook.eu1.make.com/"): AiApiService {
            val json = Json { ignoreUnknownKeys = true }
            val contentType = "application/json".toMediaType()

            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()

            return retrofit.create(AiApiService::class.java)
        }
    }
}
