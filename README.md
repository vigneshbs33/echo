# Echo AI Writing Assistant

Echo is an Android-first scaffold for a "ghost layer" writing assistant, offering text capture, overlay functionality, and direct text rewriting using AI.

## Features
- **Accessibility Service**: Captures onscreen text and replaces it after processing.
- **Floating Overlay Bubble**: Gives users quick access to the assistant anytime.
- **Tone Adjustments**: Prompt construction for tone-based text rewriting.
- **OpenAI Integration**: Direct-to-OpenAI client wiring.

## Project Structure
- `apps/android`: Main Android application module
- `core/model`: Shared product models
- `core/prompt`: Prompt factory and tone rules
- `core/network`: OpenAI client scaffolding
- `core/designsystem`: Shared Compose theme
- `feature/onboarding`: First-run setup and status UI

## How to Build & Replicate
1. Clone the repository:
   ```bash
   git clone https://github.com/vigneshbs33/echo.git
   ```
2. Open the project in Android Studio.
3. You may need to create a `local.properties` file in the root directory. You can use the `local.properties.example` file as a reference and provide your `sdk.dir` path.
4. Sync the Gradle project to download all dependencies.
5. Build and run the `apps:android` configuration on an emulator or physical device.

## Current Status
This is a foundation for an AI writing companion application, ready for additional features like storing secure API keys and refining the UI components with Jetpack Compose.
