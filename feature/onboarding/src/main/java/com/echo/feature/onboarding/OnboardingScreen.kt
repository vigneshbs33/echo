package com.echo.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
internal fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    versionName: String,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onStartBackgroundService: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Re-check permissions whenever we return to the screen
    LaunchedEffect(Unit) {
        viewModel.checkPermissions(context)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { HeroSection(versionName = versionName) }
            item {
                ApiKeyCard(
                    apiKey = state.apiKey,
                    apiKeySaved = state.apiKeySaved,
                    validationError = state.validationError,
                    onApiKeyChanged = viewModel::onApiKeyChanged,
                    onSave = viewModel::saveApiKey,
                    onClear = viewModel::clearApiKey,
                )
            }
            item {
                PermissionsCard(
                    hasAccessibility = state.hasAccessibilityPermission,
                    hasOverlay = state.hasOverlayPermission,
                    onOpenAccessibilitySettings = {
                        onOpenAccessibilitySettings()
                    },
                    onOpenOverlaySettings = {
                        onOpenOverlaySettings()
                    },
                    onRefresh = { viewModel.checkPermissions(context) },
                )
            }
            item {
                LaunchCard(
                    enabled = state.apiKeySaved &&
                        state.hasAccessibilityPermission &&
                        state.hasOverlayPermission,
                    serviceStarted = state.serviceStarted,
                    onStart = {
                        onStartBackgroundService()
                        viewModel.onServiceStarted()
                    },
                )
            }
            item { ToneSection() }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun HeroSection(versionName: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF6366F1), // Indigo
                            Color(0xFFA855F7), // Purple
                            Color(0xFFEC4899)  // Pink
                        )
                    ),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.echo.core.designsystem.R.drawable.ic_echo_logo),
                contentDescription = "Echo Logo",
                modifier = Modifier.size(56.dp)
            )
        }
        Text(
            text = "Echo",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = "Type messy.\nLook like a genius.",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.70f),
            textAlign = TextAlign.Center,
            lineHeight = 28.sp
        )
        Text(
            text = "v$versionName",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ApiKeyCard(
    apiKey: String,
    apiKeySaved: Boolean,
    validationError: String?,
    onApiKeyChanged: (String) -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit,
) {
    var showKey by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    EchoCard(title = "1. Your OpenAI Key 🔑") {
        Text(
            text = "Echo sends text directly from your phone to OpenAI. Your key is encrypted on-device — we never see it.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
        Spacer(Modifier.height(12.dp))

        if (!apiKeySaved) {
            OutlinedTextField(
                value = apiKey,
                onValueChange = onApiKeyChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("sk-...") },
                singleLine = true,
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    onSave()
                }),
                trailingIcon = {
                    IconButton(onClick = { showKey = !showKey }) {
                        Text(if (showKey) "🙈" else "👁️", fontSize = 18.sp)
                    }
                },
                isError = validationError != null,
                supportingText = validationError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { focusManager.clearFocus(); onSave() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save & Encrypt Key")
            }
        } else {
            StatusRow(label = "API Key", ok = true, text = "Saved & encrypted ✓")
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onClear,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("Clear Key")
            }
        }
    }
}

@Composable
private fun PermissionsCard(
    hasAccessibility: Boolean,
    hasOverlay: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onRefresh: () -> Unit,
) {
    EchoCard(title = "2. Permissions 🔒") {
        Text(
            text = "Echo needs two permissions to work across your apps:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
        Spacer(Modifier.height(12.dp))

        StatusRow(
            label = "Accessibility Service",
            ok = hasAccessibility,
            text = if (hasAccessibility) "Enabled ✓" else "Needed to read & replace text",
        )
        if (!hasAccessibility) {
            Spacer(Modifier.height(6.dp))
            FilledTonalButton(onClick = onOpenAccessibilitySettings, modifier = Modifier.fillMaxWidth()) {
                Text("Enable Accessibility")
            }
        }

        Spacer(Modifier.height(10.dp))

        StatusRow(
            label = "Draw Over Other Apps",
            ok = hasOverlay,
            text = if (hasOverlay) "Enabled ✓" else "Needed for the floating bubble",
        )
        if (!hasOverlay) {
            Spacer(Modifier.height(6.dp))
            FilledTonalButton(onClick = onOpenOverlaySettings, modifier = Modifier.fillMaxWidth()) {
                Text("Grant Overlay Permission")
            }
        }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
            Text("↻  Re-check Permissions")
        }
    }
}

@Composable
private fun LaunchCard(
    enabled: Boolean,
    serviceStarted: Boolean,
    onStart: () -> Unit,
) {
    EchoCard(title = "3. Launch Echo 🚀") {
        AnimatedContent(
            targetState = serviceStarted,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            label = "launch_state",
        ) { started ->
            if (started) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("✅", fontSize = 40.sp)
                    Text(
                        text = "Echo is active!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Tap the floating \"E\" bubble in any app, pick a tone, and watch your text transform.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!enabled) {
                        Text(
                            text = "Complete steps 1 and 2 above first.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                    Button(
                        onClick = onStart,
                        enabled = enabled,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text("Start Echo")
                    }
                }
            }
        }
    }
}

@Composable
private fun ToneSection() {
    EchoCard(title = "Tone Modes") {
        val tones = listOf(
            Triple("👔", "Professional", "Polished, formal, authoritative"),
            Triple("👋", "Friendly", "Warm, approachable, conversational"),
            Triple("🔥", "Edgy", "Bold, punchy, modern energy"),
        )
        tones.forEach { (emoji, name, desc) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(emoji, fontSize = 24.sp)
                Column {
                    Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusRow(label: String, ok: Boolean, text: String) {
    val color by animateColorAsState(
        targetValue = if (ok) Color(0xFF22C55E) else MaterialTheme.colorScheme.secondary,
        label = "status_color",
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Column {
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
        }
    }
}

@Composable
private fun EchoCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}
