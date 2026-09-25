import SwiftUI

struct RootView: View {
    @EnvironmentObject private var manager: WebViewManager
    var body: some View {
        BrowserView(manager: manager)
        .task { await manager.updateUserscript() }
    }
}
