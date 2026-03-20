package com.echo.core.network

import com.echo.core.model.EchoTone
import com.echo.core.prompt.EchoPromptFactory
import com.echo.core.prompt.RewriteLength

class OpenAiRepository(
    private val apiKey: String,
    private val promptFactory: EchoPromptFactory = EchoPromptFactory(),
) {
    private val api by lazy { OpenAiClientFactory.create(apiKey) }

    /**
     * Rewrites [text] in the given [tone] and returns the polished result.
     * Throws on network or API errors.
     */
    suspend fun rewrite(
        text: String,
        tone: EchoTone,
        customInstruction: String = "",
        length: RewriteLength = RewriteLength.SIMILAR,
        appContext: String? = null,
    ): String {
        val prompt = promptFactory.buildRewritePrompt(
            input = text,
            tone = tone,
            appContext = appContext,
            length = length,
            customInstruction = customInstruction,
        )

        val request = OpenAiRequest(
            model = "gpt-4o-mini",
            messages = listOf(
                OpenAiMessage(role = "system", content = prompt.systemPrompt),
                OpenAiMessage(role = "user", content = prompt.userPrompt),
            ),
        )

        val response = api.createChatCompletion(request)
        if (!response.isSuccessful) {
            val errMsg = response.errorBody()?.string() ?: "HTTP ${response.code()}"
            throw RuntimeException("OpenAI error: $errMsg")
        }

        val body = response.body()
        val apiError = body?.error
        if (apiError != null) {
            throw RuntimeException("OpenAI API error: ${apiError.message}")
        }

        return body?.choices?.firstOrNull()?.message?.content?.trim()
            ?: throw RuntimeException("Empty response from OpenAI")
    }

    suspend fun generateReply(
        contextText: String,
        replyIntent: EchoTone,
        customInstruction: String = "",
    ): String {
        val prompt = promptFactory.buildReplyPrompt(contextText, replyIntent, customInstruction)
        val request = OpenAiRequest(
            model = "gpt-4o-mini",
            messages = listOf(
                OpenAiMessage(role = "system", content = prompt.systemPrompt),
                OpenAiMessage(role = "user", content = prompt.userPrompt),
            ),
        )

        val response = api.createChatCompletion(request)
        if (!response.isSuccessful) {
            val errMsg = response.errorBody()?.string() ?: "HTTP ${response.code()}"
            throw RuntimeException("OpenAI error: $errMsg")
        }

        val body = response.body()
        val apiError = body?.error
        if (apiError != null) {
            throw RuntimeException("OpenAI API error: ${apiError.message}")
        }

        return body?.choices?.firstOrNull()?.message?.content?.trim()
            ?: throw RuntimeException("Empty response from OpenAI")
    }
}
