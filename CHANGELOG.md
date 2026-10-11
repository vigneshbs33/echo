# Changelog

All notable changes to Echo are listed here.

## Unreleased

### Fixed

- Undo after polishing a selection no longer wipes the rest of the field. Echo now snapshots the whole field and its selection before every edit and restores both.
- Smart Reply now writes in your voice: it uses your Smart Reply writing sample, or the sample of the chosen tone when that one is empty.

### Changed

- Editing logic moved into `core/domain`, a pure Kotlin module with `EchoEditor` and the `TextFieldGateway`, `WritingEngine`, `ApiKeyStorage` and `SettingsStorage` interfaces.
- OpenAI is now the default `WritingEngine` (`OpenAiWritingEngine`), built once with a shared HTTP client instead of per request.
- The model endpoint and model name come from Gradle properties (`echo.llm.baseUrl`, `echo.llm.model`) through `BuildConfig`.
- Manual dependency injection through `AppContainer`.
- `core/model`, `core/prompt` and `core/network` are plain JVM modules.
- Engine errors carry the HTTP status, so invalid key and rate limit messages are reliable.
- Release builds keep Gson's annotated fields.

### Added

- Unit tests for the editor, field snapshots, writing samples, prompts, Fact Check parsing, the OpenAI engine (MockWebServer) and bubble messages.
- GitHub Actions CI: ktlint, unit tests and a debug build.
- `.editorconfig` with the ktlint rules.
- README sections on architecture, integrating the engine and extending Echo.

### Earlier in this cycle

- Demo video and preview GIF in `docs/media`, shown at the top of the README.
- Rewritten README with architecture diagram, features, privacy notes and install steps.
- KDoc on the main classes, CONTRIBUTING guide, issue templates and this changelog.

## 3.0.3

The build in `android_apk_output/Echo-v3.0.3.apk`.

- Instant Undo: the original text is kept so one tap restores it.
- Fact Check tone: fixes spelling, grammar and facts without changing your style, and leaves the text alone when nothing needs fixing.
- Compact bubble menu with all tones in a small grid.
- Smart Reply from the clipboard with Accept, Decline and Clarify.
- Per tone writing samples so rewrites match your voice.
- API key stored with EncryptedSharedPreferences.
- Reworked onboarding screen.
