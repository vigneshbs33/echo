# Echo: Error Handling & Resilience

## 1. System-Level Fault Tolerance (The "Zero-Impact" Rule)
Because Echo operates as a system layer, its failure should **never** crash the user's active application or cause permanent text loss. 

---

## 2. Common Failures & Mitigation Strategies

### A. The "Capture Hack" Failure
*   **Scenario:** Echo sends `Ctrl + A` and `Ctrl + C`, but for some reason, the clipboard doesn't update or the app blocks it.
*   **Mitigation:** 
    1. Echo always checks the clipboard timestamp *before* and *after* the simulated copy. 
    2. If no change is detected within 50ms, Echo will attempt a secondary "Focus & Selection" via the Accessibility API (UIA on Windows).
    3. If all else fails, Echo will show a tiny "Manual Selection Needed" tooltip.

### B. The "API Lag" Issue
*   **Scenario:** The user hits `Alt + Space`, and the OpenAI API takes 5 seconds to respond.
*   **Mitigation:**
    1. The **Pulse Bar** will show a "Subtle Progress Shimmer" (a slow, pulsing light) to indicate that it's waiting for the "Brain."
    2. A "Cancel" button (or hitting `Esc`) will instantly dismiss the bar and stop the API call.

### C. The "Text Loss" Nightmare
*   **Scenario:** The user hits "Polish," Echo deletes the old text, but the AI response fails (e.g., no internet).
*   **Mitigation:** 
    1. **Undo Buffer:** Echo maintains its own "Last Action" buffer. 
    2. **Keyboard Restore:** If an error occurs, Echo will instantly re-paste the *original* text from its memory buffer.
    3. **Ctrl + Z Support:** Since Echo uses standard `Ctrl + V`, the user can almost always hit `Ctrl + Z` in their app to revert.

### D. The "Internet Outage" Failure
*   **Scenario:** User is on a plane or has bad Wi-Fi.
*   **Mitigation:** 
    1. Echo will perform a 100ms "Connectivity Check" before calling the API. 
    2. If offline, the **Pulse Bar** will turn a soft red and show: "Echo is offline. Fix later."

---

## 3. API Key & Model Errors
*   **Invalid Key:** If the OpenAI API returns a 401 (Unauthorized), Echo will open the **Settings Panel** and highlight the API Key field with a "Check your key" message.
*   **Token Limit:** If the text is too long (e.g., the user accidentally selects an entire 50-page PDF), Echo will show a "Too much text for one Echo" warning and refuse to process.

---

## 4. Crash Recovery
*   **Background Process:** Echo's "Listener" lives in a separate, lightweight process from the "UI Overlay." 
*   **Auto-Restart:** If the UI process crashes, the Listener will detect it and restart it automatically, ensuring `Alt + Space` always works.
