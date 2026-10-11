package com.echo.core.domain.storage

import com.echo.core.model.EchoTone

/** Picks the right writing sample for each action. */
class StyleSamples(private val storage: SettingsStorage) {

    /** The sample the user wrote for [tone], or blank. */
    fun forRewrite(tone: EchoTone): String = storage.getCustomInstruction(tone.name).trim()

    /**
     * The Smart Reply sample. Falls back to the sample of the reply's tone when the user
     * has not written a Smart Reply sample, so replies still sound like them.
     */
    fun forReply(intent: EchoTone): String =
        storage.getCustomInstruction(REPLY_KEY).trim().ifBlank { forRewrite(intent) }

    companion object {
        /** Storage key of the Smart Reply sample written in onboarding. */
        const val REPLY_KEY = "REPLY"
    }
}
