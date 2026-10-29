import Foundation
import Combine
import FirebaseAuth
import FirebaseFirestore

struct BoardTask: Identifiable {
    let id: String
    let title: String
    let completed: Bool
}

@MainActor
final class TaskBoardStore: ObservableObject {
    @Published var user: User?
    @Published var tasks: [BoardTask] = []
    @Published var checkingAuth = true
    @Published var signingIn = false
    @Published var loadingTasks = false
    @Published var savingTask = false
    @Published var updatingIDs: Set<String> = []
    @Published var message = ""
    @Published var tasksError = ""
    @Published var title = ""

    private var authListener: AuthStateDidChangeListenerHandle?
    private var taskListener: ListenerRegistration?
    private var generation = UUID()

    func start() {
        guard authListener == nil else { return }
        authListener = Auth.auth().addStateDidChangeListener { [weak self] _, user in
            DispatchQueue.main.async {
                guard let self else { return }
                self.user = user
                self.checkingAuth = false
                self.title = ""
                self.message = ""
                self.listenToTasks()
            }
        }
    }

    func stop() {
        if let authListener { Auth.auth().removeStateDidChangeListener(authListener) }
        authListener = nil
        taskListener?.remove()
        taskListener = nil
        generation = UUID()
    }

    func signIn(email: String, password: String) {
        let email = email.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !email.isEmpty, !password.isEmpty else {
            message = "Enter your email and password."
            return
        }
        guard !signingIn else { return }
        signingIn = true
        message = ""
        Auth.auth().signIn(withEmail: email, password: password) { [weak self] _, error in
            DispatchQueue.main.async {
                self?.signingIn = false
                if let error {
                    let authError = error as NSError
                    let code = authError.code
                    if code == 17995 {
                        let reason = authError.localizedFailureReason ?? "No additional Keychain details were provided."
                        self?.message = "Keychain access failed (17995). On Mac, select your development team and enable Keychain Sharing under Signing & Capabilities, then run the signed app. \(reason)"
                    } else {
                        self?.message = "Sign-in failed (Firebase Auth \(code)): \(error.localizedDescription)"
                    }
                }
            }
        }
    }

    func signOut() {
        do {
            try Auth.auth().signOut()
            // Clear shared data immediately; the auth listener resets the subscription.
            taskListener?.remove()
            generation = UUID()
            tasks = []
            user = nil
        } catch {
            message = "Could not sign out: \(error.localizedDescription)"
        }
    }

    func listenToTasks() {
        taskListener?.remove()
        taskListener = nil
        generation = UUID()
        let currentGeneration = generation
        tasks = []
        tasksError = ""
        guard user != nil else {
            loadingTasks = false
            return
        }
        loadingTasks = true
        taskListener = Firestore.firestore().collection("tasks")
            .order(by: "createdAt", descending: true)
            .addSnapshotListener { [weak self] snapshot, error in
                DispatchQueue.main.async {
                    guard let self, self.generation == currentGeneration else { return }
                    self.loadingTasks = false
                    if let error {
                        self.tasksError = "Could not load tasks: \(error.localizedDescription)"
                        return
                    }
                    guard let snapshot else {
                        self.tasksError = "No task data received. Please retry."
                        return
                    }
                    var result: [BoardTask] = []
                    for document in snapshot.documents {
                        let data = document.data()
                        guard let title = data["title"] as? String,
                              let completed = data["completed"] as? Bool else {
                            self.tasksError = "A task has invalid fields. Check the tasks collection."
                            return
                        }
                        result.append(BoardTask(id: document.documentID, title: title, completed: completed))
                    }
                    self.tasks = result
                    self.tasksError = ""
                }
            }
    }

    func addTask() {
        guard let uid = Auth.auth().currentUser?.uid else {
            message = "Sign in before adding a task."
            return
        }
        let trimmedTitle = title.trimmingCharacters(in: .whitespacesAndNewlines)
        // Count Unicode scalars to match the web client's Array.from title check.
        guard (1...80).contains(trimmedTitle.unicodeScalars.count) else {
            message = "Task titles must contain 1–80 characters after trimming whitespace."
            return
        }
        guard !savingTask else { return }
        savingTask = true
        message = ""
        let currentGeneration = generation
        Firestore.firestore().collection("tasks").addDocument(data: [
            "title": trimmedTitle,
            "completed": false,
            "createdAt": FieldValue.serverTimestamp(),
            "createdBy": uid
        ]) { [weak self] error in
            DispatchQueue.main.async {
                guard let self else { return }
                self.savingTask = false
                guard self.generation == currentGeneration else { return }
                if let error {
                    self.message = "Could not create task: \(error.localizedDescription)"
                } else {
                    self.title = ""
                    self.message = "Task saved to Firebase."
                }
            }
        }
    }

    func toggle(_ task: BoardTask) {
        guard Auth.auth().currentUser != nil, !updatingIDs.contains(task.id) else { return }
        updatingIDs.insert(task.id)
        message = ""
        let currentGeneration = generation
        // Update only completion, preserving title, creation time, and creator.
        Firestore.firestore().collection("tasks").document(task.id)
            .updateData(["completed": !task.completed]) { [weak self] error in
                DispatchQueue.main.async {
                    guard let self else { return }
                    self.updatingIDs.remove(task.id)
                    guard self.generation == currentGeneration else { return }
                    if let error {
                        self.message = "Could not update task: \(error.localizedDescription)"
                    }
                }
            }
    }
}
