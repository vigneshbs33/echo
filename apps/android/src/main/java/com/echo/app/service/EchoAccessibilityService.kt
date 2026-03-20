package com.echo.app.service

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class EchoAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        _instance = java.lang.ref.WeakReference(this)
        Log.i(TAG, "Echo Accessibility Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Capture is done on-demand, not on every event, to save battery.
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        if (_instance?.get() === this) _instance?.clear()
        super.onDestroy()
    }

    /**
     * Returns the text in the currently focused editable text field.
     * If the user has text selected, only the selected portion is returned.
     * Returns null if no suitable text field is in focus.
     */
    fun captureText(): String? {
        val node = focusedEditableNode() ?: return null
        val text = node.text?.toString() ?: return null
        if (text.isBlank()) return null

        val start = node.textSelectionStart
        val end = node.textSelectionEnd

        return if (start >= 0 && end >= 0 && start != end && start <= text.length && end <= text.length) {
            val min = minOf(start, end)
            val max = maxOf(start, end)
            text.substring(min, max).takeIf { it.isNotBlank() }
        } else {
            text
        }
    }

    /**
     * Replaces text in the focused editable field.
     * If the user had a selection, only the selected range is replaced.
     * Returns true on success.
     */
    fun replaceText(updatedText: CharSequence): Boolean {
        val node = focusedEditableNode() ?: return false
        val start = node.textSelectionStart
        val end = node.textSelectionEnd
        val original = node.text?.toString() ?: ""

        val newFullText = if (start >= 0 && end >= 0 && start != end &&
            start <= original.length && end <= original.length) {
            val min = minOf(start, end)
            val max = maxOf(start, end)
            original.substring(0, min) + updatedText + original.substring(max)
        } else {
            updatedText
        }

        val args = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, newFullText)
        }
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    private fun focusedEditableNode(): AccessibilityNodeInfo? {
        if (EchoForegroundService.isIncognito) return null
        val root = rootInActiveWindow ?: return null
        val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: return null
        if (!focused.isEditable || focused.isPassword) return null
        return focused
    }

    companion object {
        private const val TAG = "EchoAccessibility"

        @Volatile
        private var _instance: java.lang.ref.WeakReference<EchoAccessibilityService>? = null
        val instance: EchoAccessibilityService? get() = _instance?.get()
    }
}
