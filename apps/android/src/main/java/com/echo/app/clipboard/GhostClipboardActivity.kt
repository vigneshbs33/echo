package com.echo.app.clipboard

import android.app.Activity
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import com.echo.app.service.EchoForegroundService

/**
 * A completely transparent, invisible Activity that launches for ~50ms
 * purely to legally steal focus and bypass Android 10+ background clipboard restrictions.
 */
class GhostClipboardActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val clip = getSystemService(ClipboardManager::class.java).primaryClip
        var contextText = ""
        if (clip != null && clip.itemCount > 0) {
            contextText = clip.getItemAt(0).text?.toString().orEmpty()
        }
        
        val toneName = intent.getStringExtra("TONE_NAME").orEmpty()
        
        // Route the safely extracted text back to the background service!
        val serviceIntent = Intent(this, EchoForegroundService::class.java).apply {
            action = "PROCESS_SMART_REPLY_FROM_GHOST"
            putExtra("CLIPBOARD_TEXT", contextText)
            putExtra("TONE_NAME", toneName)
        }
        startService(serviceIntent)
        
        finish()
        overridePendingTransition(0, 0)
    }
}
