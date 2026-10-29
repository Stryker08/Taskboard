import SwiftUI
import FirebaseAuth

struct ContentView: View {
    @StateObject private var board = TaskBoardStore()
    @State private var email = ""
    @State private var password = ""
    private let isPreview: Bool

    init(isPreview: Bool = false) {
        self.isPreview = isPreview
    }

    var body: some View {
        NavigationStack {
            List {
                Section {
                    Text("A simple shared to-do list.")
                        .foregroundStyle(.secondary)
                    if board.checkingAuth {
                        ProgressView("Loading...")
                    }
                }

                if !board.checkingAuth {
                    if let user = board.user {
                        Section {
                            Text(user.email ?? "Signed in")
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                        }
                        Section("New task") {
                            TextField("What needs to get done?", text: $board.title)
                                .disabled(board.savingTask)
                                .onSubmit { board.addTask() }
                            Button {
                                board.addTask()
                            } label: {
                                HStack {
                                    Text(board.savingTask ? "Saving..." : "Add task")
                                    Spacer()
                                    Image(systemName: "plus")
                                }
                            }
                            .disabled(board.savingTask)
                            Text("1–80 characters after trimming whitespace.")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                        Section {
                            if board.loadingTasks {
                                ProgressView("Loading tasks...")
                            } else if !board.tasksError.isEmpty {
                                Text(board.tasksError).foregroundStyle(.red)
                                Button("Retry") { board.listenToTasks() }
                            } else if board.tasks.isEmpty {
                                Text("No tasks yet. Add one above.")
                                    .foregroundStyle(.secondary)
                            } else {
                                ForEach(board.tasks) { task in
                                    Button {
                                        board.toggle(task)
                                    } label: {
                                        HStack(spacing: 12) {
                                            Image(systemName: task.completed ? "checkmark.circle.fill" : "circle")
                                                .foregroundStyle(task.completed ? Color.blue : Color.secondary)
                                                .font(.title2)
                                            Text(task.title)
                                                .strikethrough(task.completed)
                                                .foregroundStyle(task.completed ? Color.secondary : Color.primary)
                                            Spacer()
                                            if board.updatingIDs.contains(task.id) {
                                                ProgressView()
                                            }
                                        }
                                        .padding(.vertical, 4)
                                        .contentShape(Rectangle())
                                    }
                                    .buttonStyle(.plain)
                                    .disabled(board.updatingIDs.contains(task.id))
                                    .accessibilityLabel(task.title)
                                    .accessibilityValue(task.completed ? "Completed" : "Incomplete")
                                    .accessibilityHint("Double tap to change completion")
                                }
                            }
                        } header: {
                            HStack {
                                Text("Tasks")
                                Spacer()
                                if !board.loadingTasks && board.tasksError.isEmpty {
                                    Text("\(board.tasks.filter { $0.completed }.count) / \(board.tasks.count) done")
                                }
                            }
                        }
                    } else {
                        Section("Sign in") {
                            TextField("Email", text: $email)
                                .textContentType(.username)
                                .autocorrectionDisabled()
                                #if os(iOS)
                                .textInputAutocapitalization(.never)
                                .keyboardType(.emailAddress)
                                #endif
                            SecureField("Password", text: $password)
                                .textContentType(.password)
                            Button(board.signingIn ? "Signing in..." : "Sign in") {
                                board.signIn(email: email, password: password)
                            }
                            .disabled(board.signingIn || isPreview)
                            if isPreview {
                                Text("Canvas preview only. Run the app in an iPhone simulator to sign in.")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                            }
                        }
                    }
                }

                if !board.message.isEmpty {
                    Section {
                        Text(board.message)
                            .font(.subheadline)
                            .accessibilityLabel("Status: \(board.message)")
                    }
                }
            }
            .navigationTitle("Task board")
            .toolbar {
                if board.user != nil {
                    ToolbarItem(placement: .automatic) {
                        Button("Log out") { board.signOut() }
                    }
                }
            }
        }
        .tint(.blue)
        .onAppear {
            if isPreview {
                board.checkingAuth = false
            } else {
                board.start()
            }
        }
        .onDisappear { board.stop() }
        .onChange(of: board.user?.uid) { _, uid in
            if uid != nil { password = "" }
        }
    }
}

#Preview("Sign in") {
    ContentView(isPreview: true)
}
