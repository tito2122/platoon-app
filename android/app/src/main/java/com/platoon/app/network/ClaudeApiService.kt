package com.platoon.app.network

import com.platoon.app.model.TaskAssignment
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

data class AnalyzeTaskRequest(
    val base64: String,
    val mimeType: String
)

data class AnalyzeTaskResponse(
    val assignments: List<TaskAssignment> = emptyList()
)

interface ClaudeApiService {

    @POST("analyzeTask")
    suspend fun analyzeTask(@Body request: AnalyzeTaskRequest): AnalyzeTaskResponse

    companion object {
        // Firebase Cloud Function base URL - update with your actual project URL
        private const val BASE_URL = "https://europe-west1-YOUR_PROJECT_ID.cloudfunctions.net/"

        fun create(): ClaudeApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ClaudeApiService::class.java)
        }
    }
}
