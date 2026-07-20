import SwiftUI

/// Root view for the iOS app.
/// TODO: Embed the shared KMP framework and mirror the Android navigation flow here.
struct ContentView: View {
    var body: some View {
        AuthView()
    }
}

// ── Auth ─────────────────────────────────────────────────────────────────────

struct AuthView: View {
    @State private var isSignedIn = false
    @State private var isGuest = false

    var body: some View {
        if isSignedIn || isGuest {
            DiscoverView(isGuest: isGuest)
        } else {
            authContent
        }
    }

    var authContent: some View {
        VStack(spacing: 16) {
            Spacer()
            Text("Вход")
                .font(.largeTitle)
                .bold()
                .foregroundColor(.pink)

            Text("Войдите, чтобы записываться к мастерам,\nсохранять записи и оставлять отзывы")
                .font(.subheadline)
                .multilineTextAlignment(.center)
                .foregroundColor(.secondary)

            Spacer().frame(height: 32)

            authButton(title: "Продолжить с Google") { isSignedIn = true }
            authButton(title: "Продолжить с Apple") { isSignedIn = true }
            authButton(title: "Продолжить по email") { isSignedIn = true }

            Button("Продолжить как гость") { isGuest = true }
                .foregroundColor(.pink)

            Spacer()
        }
        .padding(.horizontal, 32)
    }

    @ViewBuilder
    func authButton(title: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .frame(maxWidth: .infinity)
                .padding()
                .background(Color.pink)
                .foregroundColor(.white)
                .cornerRadius(10)
        }
    }
}

// ── Discover (stub) ───────────────────────────────────────────────────────────

struct DiscoverView: View {
    let isGuest: Bool

    var body: some View {
        NavigationView {
            VStack(spacing: 12) {
                Text("Найдите своего мастера")
                    .font(.title2)
                    .bold()
                    .padding(.top)

                Text("Выбирайте специалистов, услуги и удобное время")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)

                if isGuest {
                    Text("Вы в гостевом режиме. Запись и отзывы недоступны.")
                        .font(.caption)
                        .foregroundColor(.orange)
                        .padding()
                }

                // TODO: Implement search, category filters, featured slider, masters list
                Text("(Экран обнаружения — заглушка)")
                    .foregroundColor(.secondary)
                    .padding()

                Spacer()
            }
            .padding()
            .navigationTitle("Beauty Planner")
        }
    }
}

#Preview {
    ContentView()
}
