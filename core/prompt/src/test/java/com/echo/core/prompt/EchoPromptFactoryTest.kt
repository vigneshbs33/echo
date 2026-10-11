package com.echo.core.prompt

import com.echo.core.model.EchoTone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EchoPromptFactoryTest {

    private val factory = EchoPromptFactory()

    @Test
    fun `rewrite prompt includes the writing sample`() {
        val prompt = factory.buildRewritePrompt("hey", EchoTone.FRIENDLY, customInstruction = "yo whats up")
        assertTrue(prompt.systemPrompt.contains("yo whats up"))
        assertEquals("hey", prompt.userPrompt)
    }

    @Test
    fun `reply prompt includes the writing sample`() {
        val prompt = factory.buildReplyPrompt("Can you join?", EchoTone.PROFESSIONAL, customInstruction = "sure thing!")
        assertTrue(prompt.systemPrompt.contains("sure thing!"))
        assertEquals("Can you join?", prompt.userPrompt)
    }

    @Test
    fun `blank sample adds no style line`() {
        val prompt = factory.buildReplyPrompt("Can you join?", EchoTone.EDGY)
        assertFalse(prompt.systemPrompt.contains("natural writing style"))
    }

    @Test
    fun `fact check skips the length and style rules`() {
        val prompt = factory.buildRewritePrompt("x", EchoTone.FACT_CHECK)
        assertFalse(prompt.systemPrompt.contains(RewriteLength.SIMILAR.instruction))
    }

    @Test
    fun `promotion allows formatting`() {
        val prompt = factory.buildRewritePrompt("x", EchoTone.PROMOTION)
        assertFalse(prompt.systemPrompt.contains("Never use markdown formatting"))
    }
}
