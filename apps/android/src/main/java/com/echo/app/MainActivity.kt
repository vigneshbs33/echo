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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import android.content.Context
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column

class MainActivity : ComponentActivity() {

    private lateinit var apiKeyVault: ApiKeyVault

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiKeyVault = ApiKeyVault(applicationContext)

        setContent {
            EchoTheme {
                val prefs = getSharedPreferences("echo_crash_logs", Context.MODE_PRIVATE)
                var crashLog by remember { mutableStateOf(prefs.getString("last_crash", null)) }

                if (crashLog != null) {
                    AlertDialog(
                        onDismissRequest = { },
                        confirmButton = {
                            TextButton(onClick = { 
                                prefs.edit().remove("last_crash").apply()
                                crashLog = null 
                            }) { Text("Clear & Continue") }
                        },
                        title = { Text("App Crashed Previously") },
                        text = {
                            Column(modifier = androidx.compose.ui.Modifier.verticalScroll(rememberScrollState())) {
                                Text(crashLog ?: "")
                            }
                        }
                    )
                }

                OnboardingRoute(
                    apiKeyStorage = apiKeyVault,
                    versionName = BuildConfig.VERSION_NAME,
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
