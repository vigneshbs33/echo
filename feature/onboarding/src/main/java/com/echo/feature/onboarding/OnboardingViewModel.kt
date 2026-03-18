package com.echo.feature.onboarding

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class OnboardingState(
    val apiKey: String = "",
    val apiKeySaved: Boolean = false,
    val validationError: String? = null,
    val hasAccessibilityPermission: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val serviceStarted: Boolean = false,
)

/**
 * Simple interface so the onboarding feature module doesn't depend on the app module.
 * Implemented by ApiKeyVault in the app module.
 */
interface ApiKeyStorage {
    fun save(key: String)
    fun get(): String?
    fun clear()
}

class OnboardingViewModel(
    private val apiKeyStorage: ApiKeyStorage,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    init {
        val savedKey = apiKeyStorage.get()
        if (!savedKey.isNullOrBlank()) {
            _state.update { it.copy(apiKey = savedKey, apiKeySaved = true) }
        }
    }

    fun onApiKeyChanged(key: String) {
        _state.update { it.copy(apiKey = key, validationError = null) }
    }

    fun saveApiKey() {
        val key = _state.value.apiKey.trim()
        when {
            key.isBlank() -> _state.update { it.copy(validationError = "API key cannot be empty") }
            !key.startsWith("sk-") -> _state.update { it.copy(validationError = "Key should start with \"sk-\"") }
            else -> {
                apiKeyStorage.save(key)
                _state.update { it.copy(apiKeySaved = true, validationError = null) }
            }
        }
    }

    fun clearApiKey() {
        apiKeyStorage.clear()
        _state.update { it.copy(apiKey = "", apiKeySaved = false) }
    }

    fun checkPermissions(context: Context) {
        val hasAccessibility = isAccessibilityEnabled(context)
        val hasOverlay = Settings.canDrawOverlays(context)
        _state.update {
            it.copy(
                hasAccessibilityPermission = hasAccessibility,
                hasOverlayPermission = hasOverlay,
            )
        }
    }

    fun onServiceStarted() {
        _state.update { it.copy(serviceStarted = true) }
    }

    private fun isAccessibilityEnabled(context: Context): Boolean {
        val serviceName = "${context.packageName}/${context.packageName}.service.EchoAccessibilityService"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false
        return enabledServices.split(":").any { it.equals(serviceName, ignoreCase = true) }
    }

    class Factory(private val storage: ApiKeyStorage) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            OnboardingViewModel(storage) as T
    }
}
