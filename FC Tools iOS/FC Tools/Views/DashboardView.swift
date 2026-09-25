import SwiftUI

struct DashboardView: View {
    @ObservedObject var manager: WebViewManager
    @ObservedObject private var logger = AppLogger.shared
    let openWebApp: () -> Void
    let openSettings: () -> Void

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 22) {
                    header
                    hero
                    quickActions
                    systemStatus
                    recentActivity
                }
                .padding(.horizontal, 20)
                .padding(.top, 12)
                .padding(.bottom, 28)
            }
            .scrollIndicators(.hidden)
            .background(background)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button(action: openSettings) {
                        Image(systemName: "slider.horizontal.3")
                            .foregroundStyle(.white)
                    }
                    .accessibilityLabel("Settings")
                }
            }
        }
        .tint(FCTheme.mint)
    }

    private var background: some View {
        LinearGradient(
            colors: [FCTheme.background, Color(red: 0.03, green: 0.09, blue: 0.12)],
            startPoint: .topLeading,
            endPoint: .bottomTrailing
        )
        .ignoresSafeArea()
    }

    private var header: some View {
        HStack(alignment: .center) {
            VStack(alignment: .leading, spacing: 4) {
                Text("FC Tools")
                    .font(.largeTitle.weight(.black))
                Text("Your smarter Ultimate Team workspace")
                    .font(.subheadline)
                    .foregroundStyle(FCTheme.muted)
            }
            Spacer()
            StatusPill(title: manager.isLoading ? "Connecting" : "Ready", color: manager.isLoading ? .orange : FCTheme.mint, systemImage: manager.isLoading ? "bolt.horizontal.circle" : "checkmark.circle.fill")
        }
        .foregroundStyle(.white)
    }

    private var hero: some View {
        GlassCard {
            VStack(alignment: .leading, spacing: 16) {
                HStack {
                    Image(systemName: "sparkles")
                        .font(.title2.weight(.bold))
                        .foregroundStyle(FCTheme.mint)
                    Spacer()
                    Text("FODDER CORE")
                        .font(.caption2.weight(.bold).monospaced())
                        .foregroundStyle(FCTheme.muted)
                }
                Text("Build better squads\nin less time.")
                    .font(.title.weight(.black))
                    .foregroundStyle(.white)
                Text("Open the Web App and use Fodder tools with a clean, focused workspace.")
                    .font(.subheadline)
                    .foregroundStyle(FCTheme.muted)
                Button(action: openWebApp) {
                    Label("Open Web App", systemImage: "arrow.up.right.square.fill")
                        .font(.subheadline.weight(.bold))
                        .foregroundStyle(.black)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(FCTheme.mint, in: RoundedRectangle(cornerRadius: 15, style: .continuous))
                }
                .buttonStyle(.plain)
            }
        }
    }

    private var quickActions: some View {
        VStack(alignment: .leading, spacing: 10) {
            sectionTitle("Quick actions")
            QuickActionButton(title: "Open Ultimate Team", subtitle: "Continue where you left off", systemImage: "gamecontroller.fill", tint: FCTheme.cyan, action: openWebApp)
            QuickActionButton(title: "Refresh Fodder tools", subtitle: "Check for the latest script", systemImage: "arrow.triangle.2.circlepath", tint: FCTheme.mint) {
                Task { await manager.updateUserscript() }
            }
        }
    }

    private var systemStatus: some View {
        VStack(alignment: .leading, spacing: 10) {
            sectionTitle("System status")
            GlassCard {
                VStack(spacing: 14) {
                    statusRow("Fodder tools", value: "v\(manager.scriptVersion)", color: FCTheme.mint, icon: "checkmark.shield.fill")
                    Divider().overlay(.white.opacity(0.08))
                    statusRow("Automatic updates", value: "Enabled", color: FCTheme.mint, icon: "arrow.down.app.fill")
                    Divider().overlay(.white.opacity(0.08))
                    statusRow("Saved login", value: KeychainManager().read() == nil ? "Not configured" : "Available", color: KeychainManager().read() == nil ? .orange : FCTheme.cyan, icon: "lock.fill")
                }
            }
        }
    }

    private var recentActivity: some View {
        VStack(alignment: .leading, spacing: 10) {
            sectionTitle("Recent activity")
            GlassCard {
                if let entry = logger.entries.last {
                    HStack(alignment: .top, spacing: 12) {
                        Image(systemName: entry.level == .error ? "exclamationmark.triangle.fill" : "checkmark.circle.fill")
                            .foregroundStyle(entry.level == .error ? .red : FCTheme.mint)
                        VStack(alignment: .leading, spacing: 4) {
                            Text(entry.message).font(.subheadline).lineLimit(2)
                            Text(entry.date.formatted(date: .omitted, time: .shortened))
                                .font(.caption)
                                .foregroundStyle(FCTheme.muted)
                        }
                    }
                } else {
                    Label("Everything is ready when you are.", systemImage: "sparkles")
                        .font(.subheadline)
                        .foregroundStyle(FCTheme.muted)
                }
            }
        }
    }

    private func sectionTitle(_ title: String) -> some View {
        Text(title.uppercased())
            .font(.caption.weight(.bold).monospaced())
            .foregroundStyle(FCTheme.muted)
            .tracking(1.2)
    }

    private func statusRow(_ title: String, value: String, color: Color, icon: String) -> some View {
        HStack {
            Image(systemName: icon).foregroundStyle(color).frame(width: 22)
            Text(title).font(.subheadline).foregroundStyle(.white)
            Spacer()
            Text(value).font(.caption.weight(.semibold)).foregroundStyle(color)
        }
    }
}
