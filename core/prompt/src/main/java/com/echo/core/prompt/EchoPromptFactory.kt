package com.echo.core.prompt

import com.echo.core.model.EchoTone

class EchoPromptFactory {

    fun buildRewritePrompt(
        input: String,
        tone: EchoTone,
        appContext: String? = null,
        length: RewriteLength = RewriteLength.SIMILAR,
    ): PromptBundle {
        val contextLine = appContext
            ?.takeIf { it.isNotBlank() }
            ?.let { "App context: The user is currently typing in $it." }
            .orEmpty()

        return PromptBundle(
            systemPrompt = buildString {
                appendLine("You are Echo, a system-level writing assistant.")
                appendLine(toneInstruction(tone))
                appendLine(globalConstraints)
                appendLine(length.instruction)
                if (contextLine.isNotBlank()) {
                    appendLine(contextLine)
                }
            }.trim(),
            userPrompt = input.trim(),
        )
    }

    fun buildReplyPrompt(contextText: String, replyIntent: EchoTone): PromptBundle {
        val intentInstruction = when (replyIntent) {
            EchoTone.PROFESSIONAL -> "Write a positive, agreeing response to the following message."
            EchoTone.EDGY -> "Write a polite declining response to the following message."
            EchoTone.FRIENDLY -> "Write a clarifying response asking for more detail about the following message."
        }
        return PromptBundle(
            systemPrompt = """
                You are Echo, a private writing assistant.
                $intentInstruction
                Keep the response concise (1-2 sentences). Do not include any quotes, introductions, or explanations.
            """.trimIndent(),
            userPrompt = contextText.trim(),
        )
    }

    private fun toneInstruction(tone: EchoTone): String = when (tone) {
        EchoTone.PROFESSIONAL ->
            "Rewrite the input to sound polished, authoritative, concise, and formal. Remove slang and emojis."
        EchoTone.FRIENDLY ->
            "Rewrite the input to sound warm, helpful, conversational, and approachable."
        EchoTone.EDGY ->
            "Rewrite the input to sound bold, creative, high-energy, and modern without changing the facts."
    }

    private companion object {
        private val globalConstraints = """
            Always preserve the user's intent and key facts.
            Fix spelling, grammar, and punctuation errors.
            Never add introductions like "Here is your rewrite." or sign-offs.
            Never use em dashes (—). Use commas, periods, or rephrase instead.
            Never use markdown formatting (no **, no #, no bullet points unless the input had them).
            Output ONLY the rewritten text. Nothing else.
            If the input is already strong, return it with only minimal necessary changes.
        """.trimIndent()
    }
}

