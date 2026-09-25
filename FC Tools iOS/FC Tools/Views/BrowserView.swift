import SwiftUI

struct BrowserView: View {
    @ObservedObject var manager: WebViewManager
    @State private var showingSettings = false
    @AppStorage("settingsButtonOffsetX") private var settingsButtonOffsetX = 0.0
    @AppStorage("settingsButtonOffsetY") private var settingsButtonOffsetY = 0.0
    @State private var dragStartOffset = CGSize.zero
    @State private var isDraggingSettingsButton = false

    var body: some View {
        GeometryReader { geometry in
            ZStack(alignment: .topTrailing) {
                WebViewContainer(manager: manager)
                    .ignoresSafeArea()

                settingsButton
                    .position(settingsButtonPosition(in: geometry.size))
                    .zIndex(100)

                if manager.isLoading {
                    ProgressView()
                        .controlSize(.small)
                        .tint(.green)
                        .padding(.top, 40)
                        .padding(.trailing, 14)
                }

                if let error = manager.lastError {
                    ContentUnavailableView(
                        "Couldn’t Connect",
                        systemImage: "wifi.exclamationmark",
                        description: Text(error)
                    )
                    .background(.regularMaterial)
                    .padding(20)
                }

                if showingSettings {
                    Color.black.opacity(0.22)
                        .ignoresSafeArea()
                        .onTapGesture { withAnimation { showingSettings = false } }
                        .zIndex(90)

                    SettingsView(manager: manager) {
                        withAnimation { showingSettings = false }
                    }
                    .frame(maxWidth: 345, maxHeight: 520)
                    .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 22, style: .continuous))
                    .overlay {
                        RoundedRectangle(cornerRadius: 22, style: .continuous)
                            .stroke(.white.opacity(0.16), lineWidth: 1)
                    }
                    .padding(.top, 100)
                    .padding(.trailing, 10)
                    .transition(.scale(scale: 0.92, anchor: .topTrailing).combined(with: .opacity))
                    .zIndex(110)
                }
            }
        }
        .background(.black)
        .statusBarHidden(false)
    }

    private var settingsButton: some View {
        Image(systemName: showingSettings ? "xmark" : "ellipsis")
            .font(.system(size: 18, weight: .bold))
            .foregroundStyle(.white)
            .frame(width: 48, height: 44)
            .background(.black.opacity(0.68), in: Capsule())
            .contentShape(Capsule())
            .opacity(showingSettings ? 1 : 0.82)
            .onTapGesture {
                guard !isDraggingSettingsButton else { return }
                withAnimation(.easeInOut(duration: 0.18)) { showingSettings.toggle() }
            }
            .simultaneousGesture(
                DragGesture(minimumDistance: 5)
                    .onChanged { value in
                        if !isDraggingSettingsButton {
                            isDraggingSettingsButton = true
                            dragStartOffset = CGSize(width: settingsButtonOffsetX, height: settingsButtonOffsetY)
                        }
                        settingsButtonOffsetX = dragStartOffset.width + value.translation.width
                        settingsButtonOffsetY = dragStartOffset.height + value.translation.height
                    }
                    .onEnded { _ in
                        dragStartOffset = CGSize(width: settingsButtonOffsetX, height: settingsButtonOffsetY)
                        DispatchQueue.main.async { isDraggingSettingsButton = false }
                    }
            )
            .accessibilityAddTraits(.isButton)
            .accessibilityLabel("Open settings")
    }

    private func settingsButtonPosition(in size: CGSize) -> CGPoint {
        let halfWidth: CGFloat = 24
        let halfHeight: CGFloat = 22
        let baseX = size.width - halfWidth - 12
        let baseY: CGFloat = 94
        let x = min(max(baseX + settingsButtonOffsetX, halfWidth), size.width - halfWidth)
        let y = min(max(baseY + settingsButtonOffsetY, halfHeight + 8), size.height - halfHeight)
        return CGPoint(x: x, y: y)
    }
}
