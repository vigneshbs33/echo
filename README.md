# Echo

Echo is an Android app that cleans up text in any app. You type a rough draft in WhatsApp, Gmail, Slack or any other text field, tap a small floating bubble, pick a tone, and Echo replaces your draft with a polished version in the same field. One tap undoes it.

It works system wide, without a custom keyboard, by using an Android Accessibility Service to read and write the focused text field.

## What it does

- **Polish in place.** Reads the text in the focused field (or just the selected part), rewrites it with an LLM, and writes it back into the same field.
- **Tones.** Professional, Friendly, Edgy, Fact Check (fixes spelling, grammar and facts while keeping your own style) and Promo. Each tone can carry your own custom instruction.
- **Undo.** Restores your original text after any rewrite.
- **Smart Reply.** Copy a message you received, tap into your reply box, tap Smart Reply, and Echo writes a reply in the tone you pick straight into that box.
- **Leaves good text alone.** If the text needs no changes, Echo says so and does not touch the field. When it does change something, it shows a short reason.
- **Skips password fields**, and a Pause button in its notification stops all capture until you resume.

## Try it in 3 steps

1. Download [`android_apk_output/Echo-v3.0.3.apk`](android_apk_output/Echo-v3.0.3.apk) to an Android 10+ phone and install it (allow installs from your browser or file manager when asked).
2. Open Echo, paste your OpenAI API key, and grant the two permissions it asks for: **Display over other apps** and **Accessibility** (Settings > Accessibility > Echo > On).
3. Open any app, type something messy in a text field, tap the Echo bubble, and pick a tone.

## How it works

```
Floating bubble (overlay, Jetpack Compose)
        |  tap tone
        v
EchoForegroundService  --->  EchoAccessibilityService.captureText()
        |                        finds the focused editable node, reads text or selection
        v
OpenAiRepository (Retrofit, gpt-4o-mini) with a tone prompt from core/prompt
        |  returns JSON: polished text + short reason, or "all good"
        v
EchoAccessibilityService.replaceText()  ->  ACTION_SET_TEXT on the same node
```

- `EchoAccessibilityService`: reads the focused, editable, non-password field and replaces only the selected range when there is a selection. It does nothing on ordinary accessibility events, so it costs no battery while idle.
- `EchoBubbleController`: the snap-to-edge overlay bubble and tone menu, drawn with Compose inside a `WindowManager` overlay.
- `EchoForegroundService`: keeps the bubble alive, runs the capture, rewrite and replace flow, and holds the undo state.
- `GhostClipboardActivity`: an invisible activity that takes window focus for a moment, because Android 10+ only lets the focused app read the clipboard. This is what makes Smart Reply possible.
- `ApiKeyVault`: stores the API key with `EncryptedSharedPreferences` (Android Keystore, AES-256).

## Project layout

| Module | What is in it |
|---|---|
| `apps/android` | App, accessibility service, foreground service, overlay bubble, vaults |
| `feature/onboarding` | Setup and status screens (Compose) |
| `core/network` | OpenAI client and models |
| `core/prompt` | Tone prompts for rewrite and reply |
| `core/model` | Shared types such as `EchoTone` |
| `core/designsystem` | Theme |
| `Context_MD` | Early product and design notes |

## Privacy

- There is no Echo server. Text goes straight from the phone to the OpenAI API with your own key, only when you tap a tone.
- The key is encrypted on the device and never leaves it except as the auth header of that request.
- Echo reads only the field in focus, only on a tap. It never reads password fields, and Pause turns capture off.

## Build from source

Requirements: Android Studio (or the Android SDK with platform 35) and JDK 17.

```
git clone https://github.com/vigneshbs33/echo.git
cd echo
cp local.properties.example local.properties   # set sdk.dir to your Android SDK
./gradlew :apps:android:assembleDebug
```

The APK is written to `apps/android/build/outputs/apk/debug/`.

## Limits

- Some apps draw their own text views and ignore `ACTION_SET_TEXT`. Echo then shows "Could not replace text in this app" instead of failing silently.
- Rewriting needs a network connection and an OpenAI API key.

## License

MIT, see [LICENSE](LICENSE).
