# Echo: The "Write Fast, Fix Later" AI Companion 🚀

<div align="center">
  <a href="https://raw.githubusercontent.com/vigneshbs33/echo/main/android_apk_output/Echo-v3.0.3.apk">
    <img src="https://img.shields.io/badge/Download_APK_for_Android-100000?style=for-the-badge&logo=android&logoColor=white&labelColor=2C1E4A&color=00C853" alt="Download APK" />
  </a>
  <p/>
  <b>100% Open Source & Safe</b> 🛡️ — <i>Review the code, build it yourself, or download the pre-compiled APK directly. No data tracking, no ads.</i>
</div>
<br/>

Echo is a premium Android-first writing assistant providing a "ghost layer" that sits between the OS and any application. It eliminates the friction of real-time editing by allowing users to type messy drafts and instantly transform them into polished, professional, or creative prose using AI.

## ✨ Core Features
- **Ghost Layer (Accessibility Service)**: Captures text from any focused field and replaces it instantly via the Accessibility API.
- **Floating Overlay Bubble**: A non-intrusive, snap-to-edge UI for quick access to polishing features.
- **Tone Personalities**: Choose between **Professional (👔)**, **Friendly (👋)**, **Edgy (🔥)**, and **Fact Check (✅)** modes.
- **Instant Undo**: Change your mind? Revert any AI replacement instantly with a compact undo button.
- **Smart Context**: Detects incoming messages in your clipboard and suggests context-aware replies.
- **Privacy First**: All processing happens direct-to-OpenAI using **your own API Key**, which is encrypted locally on-device.

## 📱 What's New in v3.0.3
- **Promotion Tone**: A brand new ✨ Promo tone (📣) to instantly craft engaging marketing and direct-response messages.
- **Landscape Polishing**: The floating bubble menu now intelligently scroll-locks so you'll never lose the Cancel button when rotating your screen.

## 📱 What's New in v3.0.2
- **Compact UI**: The floating menu now uses a sleek 2x2 grid to save screen real-estate.
- **Icon-Only Undo**: A miniaturized ↩️ button replaces the old horizontal bar for a cleaner overlay.
- **Professional App Redesign**: The main onboarding and status screens have been redesigned with a modern, clean, and professional aesthetic.
- **Fact Check Mode**: A new tone that fixes errors without changing your authentic writing style.

## 🏗️ Project Structure
- `apps/android`: Primary Android application and system services.
- `feature/onboarding`: Jetpack Compose-based setup and status UI.
- `core/designsystem`: Premium Material 3 theme and UI tokens.
- `core/prompt`: Advanced persona-based prompt engineering.
- `core/network`: Direct OpenAI integration (gpt-4o-mini).
- `core/model`: Shared entity and domain models.

## 🚀 Getting Started
1. Clone the repository: `git clone https://github.com/vigneshbs33/echo.git`
2. Open in Android Studio.
3. Add your OpenAI API key in the app's onboarding screen (it stays on your device!).
4. Grant **Accessibility** and **Overlay** permissions.
<<<<<<< HEAD
5. Start typing messy, tap the "Bat" bubble, and look like a genius.
=======
5. Start typing messy, tap the "E" bubble, and look like a genius.
>>>>>>> 74d5d23028cb92f04290663c12f0d31170a3b297

## 📝 Documentation
For deeper technical dives, see the `Context_MD/` directory:
- [Product Idea](Context_MD/Idea_MD/product_idea.md)
- [Architecture Blueprint](Context_MD/Plan_MD/master_blueprint.md)
- [Android Implementation](Context_MD/Plan_MD/android_implementation.md)

