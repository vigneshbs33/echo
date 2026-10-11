package com.echo.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.echo.core.domain.storage.ApiKeyStorage
import com.echo.core.domain.storage.SettingsStorage

@Composable
fun OnboardingRoute(
    apiKeyStorage: ApiKeyStorage,
    settingsStorage: SettingsStorage,
    versionName: String = "",
    onOpenAccessibilitySettings: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onStartBackgroundService: () -> Unit,
) {
    val viewModel: OnboardingViewModel = viewModel(
        factory = OnboardingViewModel.Factory(apiKeyStorage, settingsStorage),
    )
    OnboardingScreen(
        viewModel = viewModel,
        versionName = versionName,
        onOpenAccessibilitySettings = onOpenAccessibilitySettings,
        onOpenOverlaySettings = onOpenOverlaySettings,
        onStartBackgroundService = onStartBackgroundService,
    )
}
