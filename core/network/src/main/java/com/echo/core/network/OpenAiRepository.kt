package com.echo.core.network

import com.echo.core.model.EchoTone
import com.echo.core.prompt.EchoPromptFactory
import com.echo.core.prompt.RewriteLength

/**
 * Result of a rewrite. [allGood] is true when the model reported that no change was
 * needed (Fact Check tone), in which case [text] is empty and the field is left as is.
 */
data class PolishResult(
    val text: String,
    val reason: String? = null,
    val allGood: Boolean = false
)

/**
 * Talks to the OpenAI Chat Completions API with the user's own key.
 *
 * [rewrite] polishes text in a tone. [generateReply] writes a reply to a copied message.
 * Both use `gpt-4o-mini` and throw on network or API errors.
 */
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
    ): PolishResult {
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
            responseFormat = if (tone == EchoTone.FACT_CHECK) ResponseFormat("json_object") else null
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

        val content = body?.choices?.firstOrNull()?.message?.content?.trim()
            ?: throw RuntimeException("Empty response from OpenAI")

        if (tone == EchoTone.FACT_CHECK) {
            return try {
                val json = org.json.JSONObject(content)
                val isAllGood = json.optBoolean("all_good", false)
                if (isAllGood) {
                    PolishResult(text = "", reason = null, allGood = true)
                } else {
                    val correctedField = json.optString("corrected_text", "")
                    val reasonField = json.optString("reason", "")
                    PolishResult(
                        text = if (correctedField.isNotBlank()) correctedField else content,
                        reason = if (reasonField.isNotBlank()) reasonField else null,
                        allGood = false
                    )
                }
            } catch (e: Exception) {
                PolishResult(content, null)
            }
        }
        return PolishResult(content, null)
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
