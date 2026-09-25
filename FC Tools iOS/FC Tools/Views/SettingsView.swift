import SwiftUI

struct SettingsView: View {
    @EnvironmentObject private var settings: SettingsManager
    @ObservedObject var manager: WebViewManager
    let close: () -> Void

    @State private var accountEmail = ""
    @State private var accountPassword = ""
    @State private var updateState = "Updates run automatically on launch"

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                HStack {
                    VStack(alignment: .leading, spacing: 3) {
                        Text("Settings").font(.headline.weight(.bold))
                        Text("Private on-device controls").font(.caption).foregroundStyle(.secondary)
                    }
                    Spacer()
                    Button(action: close) {
                        Image(systemName: "xmark.circle.fill")
                            .font(.title3)
                            .foregroundStyle(.secondary)
                    }
                }

                Divider().overlay(.white.opacity(0.12))
                Text("SAVED LOGIN").font(.caption2.weight(.bold).monospaced()).foregroundStyle(.secondary)
                TextField("EA email / username", text: $accountEmail)
                    .textContentType(.username)
                    .textInputAutocapitalization(.never)
                    .keyboardType(.emailAddress)
                    .textFieldStyle(.roundedBorder)
                SecureField("EA password", text: $accountPassword)
                    .textContentType(.password)
                    .textFieldStyle(.roundedBorder)
                Text("The fields fill automatically when the Web App login appears. FC Tools never presses Sign In.")
                    .font(.caption)
                    .foregroundStyle(.secondary)
                HStack {
                    Button("Save", systemImage: "lock.fill") { saveCredentials() }
                        .buttonStyle(.borderedProminent)
                        .tint(FCTheme.mint)
                    Button("Remove", systemImage: "trash", role: .destructive) { removeCredentials() }
                        .buttonStyle(.bordered)
                }

                Divider().overlay(.white.opacity(0.12))
                Text("UPDATES").font(.caption2.weight(.bold).monospaced()).foregroundStyle(.secondary)
                Label(updateState, systemImage: "arrow.down.app.fill")
                    .font(.caption)
                    .foregroundStyle(FCTheme.mint)
                Button("Check now", systemImage: "arrow.clockwise") {
                    Task {
                        await manager.updateUserscript()
                        updateState = "Latest Fodder tools loaded"
                    }
                }
                .buttonStyle(.bordered)
            }
            .padding(18)
        }
        .onAppear {
            if let credentials = KeychainManager().read() {
                accountEmail = credentials.email
                accountPassword = credentials.password
            }
        }
    }

    private func saveCredentials() {
        guard !accountEmail.isEmpty, !accountPassword.isEmpty else { return }
        do {
            try KeychainManager().save(email: accountEmail, password: accountPassword)
            settings.quickLoginEnabled = true
            manager.reconfigureAutofill(reload: false)
            updateState = "Login saved securely"
            AppLogger.shared.log("Saved login updated; automatic form filling is enabled.")
        } catch {
            AppLogger.shared.log(error.localizedDescription, level: .error)
        }
    }

    private func removeCredentials() {
        KeychainManager().delete()
        settings.quickLoginEnabled = false
        accountEmail = ""
        accountPassword = ""
        manager.reconfigureAutofill(reload: false)
    }
}
