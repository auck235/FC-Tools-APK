# FC Tools

Private multi-platform client for the EA FC Web App and Fodder tools.

## Platforms

- `FC Tools iOS` — SwiftUI + WKWebView, Keychain credential storage, automatic Fodder script refresh.
- `Android` — native WebView shell, Android Keystore credential storage, shared dark UI.
- `Desktop` — Electron shell for Windows/macOS/Linux, OS-encrypted credential storage and signed-release update checks.
- `shared` — small Fodder bootstrap that loads the latest official client without bundling a stale copy.

## Private distribution

- iOS: Ad Hoc distribution; only registered devices can run the IPA. See [`distribution/INSTALL-IOS-HE.md`](distribution/INSTALL-IOS-HE.md).
- Android: build the APK through `.github/workflows/android-debug.yml` or distribute privately through Google Play internal testing.
- Desktop: build the Windows installer with `pnpm install` and `pnpm dist` inside `Desktop`.

The app stores credentials only through the operating system secure storage and fills the login form without submitting it.
