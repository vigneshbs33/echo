package com.echo.app.di

import android.content.Context
import com.echo.app.BuildConfig
import com.echo.app.field.AccessibilityTextFieldGateway
import com.echo.app.vault.ApiKeyVault
import com.echo.app.vault.SettingsVault
import com.echo.core.domain.EchoEditor
import com.echo.core.domain.engine.WritingEngine
import com.echo.core.domain.field.TextFieldGateway
import com.echo.core.domain.storage.ApiKeyStorage
import com.echo.core.domain.storage.SettingsStorage
import com.echo.core.network.OpenAiConfig
import com.echo.core.network.OpenAiWritingEngine

/**
 * Builds and holds the app's single instances. This is the one place to swap an
 * implementation: point [writingEngine] at your own [WritingEngine], or [textField] at
 * your own [TextFieldGateway], and the rest of the app picks it up.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val apiKeyStorage: ApiKeyStorage by lazy { ApiKeyVault(appContext) }

    val settingsStorage: SettingsStorage by lazy { SettingsVault(appContext) }

    val writingEngine: WritingEngine by lazy {
        OpenAiWritingEngine(
            config = OpenAiConfig(
                baseUrl = BuildConfig.ECHO_LLM_BASE_URL,
                model = BuildConfig.ECHO_LLM_MODEL,
            ),
            apiKeyProvider = { apiKeyStorage.get() },
        )
    }

    val textField: TextFieldGateway = AccessibilityTextFieldGateway

    val editor: EchoEditor by lazy { EchoEditor(textField, writingEngine, settingsStorage) }
}
