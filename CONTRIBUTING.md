# Contributing to Echo

Thanks for taking a look. Bug reports, ideas and pull requests are all welcome.

## Reporting a bug

Open an issue with the bug template and include:

- Phone model and Android version
- The app you were typing in (for example WhatsApp or Gmail)
- What you tapped, what you expected and what happened
- The message shown on the bubble, if any

Please never paste your OpenAI API key or private messages into an issue.

## Making a change

1. Fork the repo and create a branch from `main`.
2. Build with `./gradlew :apps:android:assembleDebug` (see the README for setup).
3. Test on a real device: enable the Accessibility Service, then try Polish, Smart Reply and Undo in at least one chat app and one email app.
4. Keep changes focused. One feature or fix per pull request.
5. Open a pull request that explains what changed and how you tested it.

## Code style

- Kotlin with the standard Android Studio formatter.
- UI in Jetpack Compose.
- Keep the privacy rules intact: read text only on a user tap, never read password fields, never log user text, never send text anywhere except the model API the user configured.
- Never commit API keys, keystores or `local.properties`.
