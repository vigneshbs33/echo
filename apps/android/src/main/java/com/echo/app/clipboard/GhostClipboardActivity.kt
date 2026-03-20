package com.echo.app.clipboard

import android.app.Activity
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.echo.app.service.EchoForegroundService

/**
 * A completely transparent, invisible Activity that launches for ~50ms
 * purely to legally read the clipboard (bypasses Android 10+ background clipboard restrictions)
 * and route the text back to EchoForegroundService.
 */
class GhostClipboardActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val clip = getSystemService(ClipboardManager::class.java).primaryClip
        val contextText = if (clip != null && clip.itemCount > 0) {
            clip.getItemAt(0).coerceToText(this)?.toString().orEmpty()
        } else {
            ""
        }

        val toneName = intent.getStringExtra(EchoForegroundService.EXTRA_TONE_NAME).orEmpty()
        Log.d("EchoGhost", "Clipboard len=${contextText.length}, tone=$toneName")

        val serviceIntent = Intent(this, EchoForegroundService::class.java).apply {
            action = EchoForegroundService.ACTION_SMART_REPLY_FROM_GHOST
            putExtra(EchoForegroundService.EXTRA_CLIPBOARD_TEXT, contextText)
            putExtra(EchoForegroundService.EXTRA_TONE_NAME, toneName)
        }
        startService(serviceIntent)

        finish()
        overridePendingTransition(0, 0)
    }
}
