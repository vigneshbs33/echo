package com.echo.core.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface OpenAiApi {
    @POST("v1/chat/completions")
    suspend fun createChatCompletion(
        @Body body: OpenAiRequest,
    ): Response<OpenAiResponse>
}
