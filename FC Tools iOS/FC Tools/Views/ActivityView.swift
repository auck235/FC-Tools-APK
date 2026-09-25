import SwiftUI

struct ActivityView: View {
    @ObservedObject private var logger = AppLogger.shared

    var body: some View {
        NavigationStack {
            Group {
                if logger.entries.isEmpty {
                    ContentUnavailableView("No activity yet", systemImage: "waveform.path.ecg", description: Text("Updates and browser events will appear here."))
                } else {
                    List(logger.entries.reversed()) { entry in
                        HStack(alignment: .top, spacing: 12) {
                            Image(systemName: entry.level == .error ? "xmark.octagon.fill" : entry.level == .warning ? "exclamationmark.triangle.fill" : "checkmark.circle.fill")
                                .foregroundStyle(entry.level == .error ? .red : entry.level == .warning ? .orange : FCTheme.mint)
                            VStack(alignment: .leading, spacing: 4) {
                                Text(entry.message).font(.subheadline)
                                Text(entry.date.formatted(date: .abbreviated, time: .shortened))
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                            }
                        }
                        .padding(.vertical, 4)
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("Activity")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Clear") { logger.clear() }
                        .disabled(logger.entries.isEmpty)
                }
            }
        }
    }
}
