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
        Log.d(TAG, "Echo Accessibility Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // We capture text on demand (not on every event) to avoid battery drain.
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        if (_instance?.get() === this) _instance?.clear()
        super.onDestroy()
    }

    /**
     * Returns the text in the currently focused editable text field, or null if none.
     */
    fun captureText(): String? {
        val node = focusedEditableNode() ?: return null
        val start = node.textSelectionStart
        val end = node.textSelectionEnd
        val text = node.text?.toString() ?: return null

        return if (start >= 0 && end >= 0 && start != end && start <= text.length && end <= text.length) {
            val min = minOf(start, end)
            val max = maxOf(start, end)
            text.substring(min, max).takeIf { it.isNotBlank() }
        } else {
            text.takeIf { it.isNotBlank() }
        }
    }

    /**
     * Replaces the text in the currently focused editable field.
     * Returns true on success.
     */
    fun replaceText(updatedText: CharSequence): Boolean {
        val node = focusedEditableNode() ?: return false
        val start = node.textSelectionStart
        val end = node.textSelectionEnd
        val original = node.text?.toString() ?: ""

        val newFullText = if (start >= 0 && end >= 0 && start != end && start <= original.length && end <= original.length) {
            val min = minOf(start, end)
            val max = maxOf(start, end)
            original.substring(0, min) + updatedText + original.substring(max)
        } else {
            updatedText
        }

        val args = Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                newFullText,
            )
        }
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    private fun focusedEditableNode(): AccessibilityNodeInfo? {
        if (EchoForegroundService.isIncognito) {
            return null
        }
        return rootInActiveWindow
            ?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            ?.takeIf { it.isEditable && !it.isPassword }
    }

    companion object {
        private const val TAG = "EchoAccessibility"

        @Volatile
        private var _instance: java.lang.ref.WeakReference<EchoAccessibilityService>? = null
        val instance: EchoAccessibilityService? get() = _instance?.get()
    }
}
