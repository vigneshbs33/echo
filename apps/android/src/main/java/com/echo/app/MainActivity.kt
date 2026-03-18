package com.echo.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.echo.app.service.EchoForegroundService
import com.echo.app.vault.ApiKeyVault
import com.echo.core.designsystem.theme.EchoTheme
import com.echo.feature.onboarding.OnboardingRoute

class MainActivity : ComponentActivity() {

    private lateinit var apiKeyVault: ApiKeyVault

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiKeyVault = ApiKeyVault(applicationContext)

        setContent {
            EchoTheme {
                OnboardingRoute(
                    apiKeyStorage = apiKeyVault,
                    onOpenAccessibilitySettings = {
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    },
                    onOpenOverlaySettings = {
                        startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:$packageName"),
                            ),
                        )
                    },
                    onStartBackgroundService = {
                        val serviceIntent = Intent(this, EchoForegroundService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(serviceIntent)
                        } else {
                            startService(serviceIntent)
                        }
                    },
                )
            }
        }
    }
}
