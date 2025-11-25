# Tapjacking Demonstration App (Ocean Professional)

A proof-of-concept Android application demonstrating tapjacking overlays with a safe demo mode, built using Kotlin and Declarative Gradle DSL.

Features:
- Overlay permission handling (SYSTEM_ALERT_WINDOW)
- Foreground service creating an overlay (TYPE_APPLICATION_OVERLAY)
- Safe Demo Mode toggle:
  - ON: overlay consumes all touches (no passthrough; safe).
  - OFF: overlay allows passthrough taps outside focused views to demonstrate risk.
- Ocean Professional theme and minimal UI

Build:
- Ensure Android SDK 34 and JDK 17 are available
- Build: `./gradlew :app:assembleDebug`
- Install: `./gradlew :app:installDebug`

Run:
- Launch "Tapjacking Demo"
- Grant "Draw over other apps" permission when prompted via the button
- Use "Show Overlay" to start the overlay service
- Toggle "Safe Demo Mode" to switch behavior (service restarts to apply)

OEM caveats:
- Some OEMs place overlay permissions under Special access or restrict them. Look for "Draw over other apps" or "Display over other apps".

Local preview note (docs):
- You can serve minimal documentation at http://localhost:3000 by running:
  `python3 -m http.server 3000` from the `tapjacking_frontend/docs` directory.

Security note:
- This app is for demonstration and educational purposes only. Do not misuse overlays to deceive users.