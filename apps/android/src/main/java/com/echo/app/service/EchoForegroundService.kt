package com.echo.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.echo.app.clipboard.GhostClipboardActivity
import com.echo.app.overlay.EchoBubbleController
import com.echo.app.vault.ApiKeyVault
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

        bubbleController = EchoBubbleController(
            context = this,
            onToneSelected = { tone -> handlePolish(tone) },
            onReplySelected = { tone -> handleSmartReply(tone) },
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "TOGGLE_INCOGNITO") {
            isIncognito = !isIncognito
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                startForeground(
                    EchoNotificationFactory.NOTIFICATION_ID,
                    EchoNotificationFactory.build(this, isIncognito),
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(
                    EchoNotificationFactory.NOTIFICATION_ID,
                    EchoNotificationFactory.build(this, isIncognito)
                )
            }
        }

        if (intent?.action == "PROCESS_SMART_REPLY_FROM_GHOST") {
            val text = intent.getStringExtra("CLIPBOARD_TEXT").orEmpty()
            val toneName = intent.getStringExtra("TONE_NAME").orEmpty()
            val tone = try { EchoTone.valueOf(toneName) } catch (e: Exception) { EchoTone.PROFESSIONAL }
            handleSmartReply(tone, text)
        }

        if (!isIncognito) {
            val shown = bubbleController.show()
            if (!shown) {
                stopSelf()
                return START_NOT_STICKY
            }
        } else {
            bubbleController.hide()
        }
        
        return START_STICKY
    }

    override fun onDestroy() {
        bubbleController.hide()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ────────────────────────────────────────────────────────────────
    // Core polish flow: capture → AI → replace
    // ────────────────────────────────────────────────────────────────
    private fun handlePolish(tone: EchoTone) {
        scope.launch {
            val apiKey = vault.get()
            if (apiKey.isNullOrBlank()) {
                launch(Dispatchers.Main) { bubbleController.setError("No API key saved.") }
                return@launch
            }

            val accessibilityService = EchoAccessibilityService.instance
            if (accessibilityService == null) {
                launch(Dispatchers.Main) { bubbleController.setError("Accessibility service not active.") }
                return@launch
            }

            val rawText = accessibilityService.captureText()
            if (rawText.isNullOrBlank()) {
                launch(Dispatchers.Main) { bubbleController.setError("No text found. Focus a text field first.") }
                return@launch
            }

            val repo = OpenAiRepository(apiKey = apiKey)

            try {
                val polished = repo.rewrite(text = rawText, tone = tone)
                val replaced = accessibilityService.replaceText(polished)
                launch(Dispatchers.Main) {
                    if (replaced) bubbleController.setDone(polished)
                    else bubbleController.setError("Couldn't replace text in this app.")
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.e(TAG, "Polish failed", e)
                launch(Dispatchers.Main) { bubbleController.setError(e.message ?: "Unknown error") }
            }
        }
    }

    private fun handleSmartReply(tone: EchoTone, contextText: String = "") {
        scope.launch {
            if (contextText.isBlank() || contextText.length < 5) {
                launch(Dispatchers.Main) { bubbleController.setError("Copied text is too short or invalid.") }
                return@launch
            }

            val apiKey = vault.get()
            if (apiKey.isNullOrBlank()) {
                launch(Dispatchers.Main) { bubbleController.setError("No API key saved.") }
                return@launch
            }

            val accessibilityService = EchoAccessibilityService.instance
            if (accessibilityService == null) {
                launch(Dispatchers.Main) { bubbleController.setError("Accessibility not active.") }
                return@launch
            }

            val repo = OpenAiRepository(apiKey = apiKey)

            try {
                val reply = repo.generateReply(contextText = contextText, replyIntent = tone)
                val replaced = accessibilityService.replaceText(reply)
                launch(Dispatchers.Main) {
                    if (replaced) bubbleController.setDone(reply)
                    else bubbleController.setError("Couldn't insert reply into this app.")
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.e(TAG, "Smart reply failed", e)
                launch(Dispatchers.Main) { bubbleController.setError(e.message ?: "Unknown error") }
            }
        }
    }

    companion object {
        const val TAG = "EchoForegroundService"
        @Volatile
        var isIncognito: Boolean = false
    }
}
