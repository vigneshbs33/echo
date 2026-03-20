package com.echo.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun OnboardingRoute(
    apiKeyStorage: ApiKeyStorage,
    versionName: String = "",
    onOpenAccessibilitySettings: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onStartBackgroundService: () -> Unit,
) {
    val viewModel: OnboardingViewModel = viewModel(
        factory = OnboardingViewModel.Factory(apiKeyStorage),
    )
    OnboardingScreen(
        viewModel = viewModel,
        versionName = versionName,
        onOpenAccessibilitySettings = onOpenAccessibilitySettings,
        onOpenOverlaySettings = onOpenOverlaySettings,
        onStartBackgroundService = onStartBackgroundService,
    )
}
