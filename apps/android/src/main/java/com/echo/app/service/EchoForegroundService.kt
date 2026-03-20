package com.echo.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.echo.app.overlay.EchoBubbleController
import com.echo.app.vault.ApiKeyVault
import com.echo.app.vault.SettingsVault
import com.echo.core.model.EchoTone
import com.echo.core.network.OpenAiRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class EchoForegroundService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var bubbleController: EchoBubbleController
    private lateinit var vault: ApiKeyVault
    private lateinit var settingsVault: SettingsVault

    override fun onCreate() {
        super.onCreate()

        EchoNotificationFactory.ensureChannel(this)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            startForeground(
                EchoNotificationFactory.NOTIFICATION_ID,
                EchoNotificationFactory.build(this),
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(
                EchoNotificationFactory.NOTIFICATION_ID,
                EchoNotificationFactory.build(this)
            )
        }

        vault = ApiKeyVault(applicationContext)
        settingsVault = SettingsVault(applicationContext)
        bubbleController = EchoBubbleController(
            context = this,
            onToneSelected = { tone -> handlePolish(tone) },
            onReplySelected = { tone -> handleSmartReplyFromClipboard(tone) },
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: action=${intent?.action}")

        // Handle explicit actions — do NOT show the bubble for these.
        when (intent?.action) {
            ACTION_TOGGLE_INCOGNITO -> {
                isIncognito = !isIncognito
                val notif = EchoNotificationFactory.build(this, isIncognito)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    startForeground(EchoNotificationFactory.NOTIFICATION_ID, notif,
                        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
                } else {
                    startForeground(EchoNotificationFactory.NOTIFICATION_ID, notif)
                }
                if (isIncognito) bubbleController.hide() else bubbleController.show()
                return START_STICKY
            }

            ACTION_SHOW_BUBBLE -> {
                // Force-show the bubble. Hides first in case a stale view is attached.
                isIncognito = false
                bubbleController.hide()
                bubbleController.show()
                return START_STICKY
            }

            ACTION_SMART_REPLY_FROM_GHOST -> {
                val text = intent.getStringExtra(EXTRA_CLIPBOARD_TEXT).orEmpty()
                val toneName = intent.getStringExtra(EXTRA_TONE_NAME).orEmpty()
                val tone = runCatching { EchoTone.valueOf(toneName) }.getOrDefault(EchoTone.PROFESSIONAL)
                handleSmartReply(tone, text)
                return START_STICKY
            }
        }

        // Default: show the bubble (first launch or START_STICKY restart).
        if (!isIncognito) {
            val shown = bubbleController.show()
            if (!shown) {
                stopSelf()
                return START_NOT_STICKY
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        bubbleController.hide()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ──────────────────────────────────────────────────────────────────────
    // Polish flow: capture text via Accessibility → AI rewrite → inject back
    // ──────────────────────────────────────────────────────────────────────
    private fun handlePolish(tone: EchoTone) {
        Log.d(TAG, "handlePolish: tone=$tone")
        scope.launch {
            val apiKey = vault.get()
            if (apiKey.isNullOrBlank()) {
                setError("No API key saved. Open Echo to set your key.")
                return@launch
            }

            val service = EchoAccessibilityService.instance
            if (service == null) {
                setError("Accessibility service not active. Enable it in Settings.")
                return@launch
            }

            val rawText = service.captureText()
            if (rawText.isNullOrBlank()) {
                setError("No text found. Tap inside a text field first.")
                return@launch
            }

            runCatching {
                val customInstruction = settingsVault.getCustomInstruction(tone.name)
                val polished = OpenAiRepository(apiKey).rewrite(rawText, tone, customInstruction)
                val replaced = service.replaceText(polished)
                launch(Dispatchers.Main) {
                    if (replaced) bubbleController.setDone(polished)
                    else setError("Could not replace text in this app.")
                }
            }.onFailure { e ->
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.e(TAG, "Polish failed", e)
                setError(friendlyError(e))
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Smart Reply via clipboard: launched by GhostClipboardActivity
    // ──────────────────────────────────────────────────────────────────────
    private fun handleSmartReplyFromClipboard(tone: EchoTone) {
        // Route through GhostClipboardActivity so we can legally read clipboard on Android 10+
        val intent = android.content.Intent(this, com.echo.app.clipboard.GhostClipboardActivity::class.java).apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION)
            putExtra(EXTRA_TONE_NAME, tone.name)
        }
        startActivity(intent)
    }

    private fun handleSmartReply(tone: EchoTone, contextText: String) {
        Log.d(TAG, "handleSmartReply: tone=$tone textLen=${contextText.length}")
        scope.launch {
            if (contextText.isBlank()) {
                setError("Nothing on clipboard. Copy a message first, then try Smart Reply.")
                return@launch
            }

            val apiKey = vault.get()
            if (apiKey.isNullOrBlank()) {
                setError("No API key saved. Open Echo to set your key.")
                return@launch
            }

            val service = EchoAccessibilityService.instance
            if (service == null) {
                setError("Accessibility service not active. Enable it in Settings.")
                return@launch
            }

            runCatching {
                val reply = OpenAiRepository(apiKey).generateReply(contextText, tone)
                val replaced = service.replaceText(reply)
                launch(Dispatchers.Main) {
                    if (replaced) bubbleController.setDone(reply)
                    else setError("Could not insert reply into this app.")
                }
            }.onFailure { e ->
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.e(TAG, "Smart reply failed", e)
                setError(friendlyError(e))
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────
    private suspend fun setError(msg: String) {
        kotlinx.coroutines.withContext(Dispatchers.Main) {
            bubbleController.setError(msg)
        }
    }

    private fun friendlyError(e: Throwable): String {
        val msg = e.message ?: "Unknown error"
        return when {
            msg.contains("401") -> "Invalid API key. Check your key in Echo settings."
            msg.contains("429") -> "Rate limit reached. Wait a moment and try again."
            msg.contains("timeout", ignoreCase = true) ||
            msg.contains("connect", ignoreCase = true) -> "Network error. Check your internet connection."
            else -> msg.take(120)
        }
    }

    companion object {
        const val TAG = "EchoForegroundService"
        const val ACTION_TOGGLE_INCOGNITO = "TOGGLE_INCOGNITO"
        const val ACTION_SHOW_BUBBLE = "SHOW_BUBBLE"
        const val ACTION_SMART_REPLY_FROM_GHOST = "PROCESS_SMART_REPLY_FROM_GHOST"
        const val EXTRA_CLIPBOARD_TEXT = "CLIPBOARD_TEXT"
        const val EXTRA_TONE_NAME = "TONE_NAME"

        @Volatile
        var isIncognito: Boolean = false
    }
}
