package com.stryker.taskboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

data class BoardTask(val id: String, val title: String, val completed: Boolean)

data class BoardState(
    val checkingAuth: Boolean = true,
    val signedIn: Boolean = false,
    val email: String = "",
    val tasks: List<BoardTask> = emptyList(),
    val title: String = "",
    val signingIn: Boolean = false,
    val saving: Boolean = false,
    val updating: Set<String> = emptySet(),
    val loadingTasks: Boolean = false,
    val tasksError: String = "",
    val message: String = ""
)

class TaskBoardViewModel : ViewModel() {
    var state by mutableStateOf(BoardState())
        private set

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var taskListener: ListenerRegistration? = null
    private var generation = 0
    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        generation++
        taskListener?.remove()
        taskListener = null
        state = BoardState(checkingAuth = false, signedIn = user != null, email = user?.email.orEmpty())
        if (user != null) listenToTasks()
    }

    init { auth.addAuthStateListener(authListener) }

    fun setTitle(value: String) { state = state.copy(title = value) }

    fun signIn(email: String, password: String) {
        if (state.signingIn) return
        if (email.isBlank() || password.isEmpty()) {
            state = state.copy(message = "Enter your email and password.")
            return
        }
        state = state.copy(signingIn = true, message = "")
        auth.signInWithEmailAndPassword(email.trim(), password).addOnCompleteListener { result ->
            state = state.copy(signingIn = false)
            if (!result.isSuccessful) {
                val error = result.exception
                val code = (error as? FirebaseAuthException)?.errorCode ?: "connection-error"
                state = state.copy(message = "Sign-in failed ($code): ${error?.localizedMessage ?: "Please try again."}")
            }
        }
    }

    fun signOut() { auth.signOut() }

    fun listenToTasks() {
        taskListener?.remove()
        generation++
        val activeGeneration = generation
        if (auth.currentUser == null) return
        state = state.copy(loadingTasks = true, tasksError = "")
        taskListener = db.collection("tasks").orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (generation != activeGeneration) return@addSnapshotListener
                if (error != null) {
                    state = state.copy(loadingTasks = false, tasksError = "Could not load tasks (${error.code}). Check your connection and Firestore permissions.")
                    return@addSnapshotListener
                }
                if (snapshot == null) {
                    state = state.copy(loadingTasks = false, tasksError = "No task data received. Please retry.")
                    return@addSnapshotListener
                }
                val tasks = mutableListOf<BoardTask>()
                for (document in snapshot.documents) {
                    val title = document.data?.get("title") as? String
                    val completed = document.data?.get("completed") as? Boolean
                    if (title == null || completed == null) {
                        state = state.copy(loadingTasks = false, tasksError = "A task has invalid fields. Check the tasks collection.")
                        return@addSnapshotListener
                    }
                    tasks.add(BoardTask(document.id, title, completed))
                }
                state = state.copy(tasks = tasks, loadingTasks = false, tasksError = "")
            }
    }

    fun addTask() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            state = state.copy(message = "Sign in before adding a task.")
            return
        }
        if (state.saving) return
        val title = state.title.trim()
        if (!isValidTaskTitle(title)) {
            state = state.copy(message = "Task titles must contain 1–80 characters after trimming whitespace.")
            return
        }
        val activeGeneration = generation
        state = state.copy(saving = true, message = "")
        db.collection("tasks").add(mapOf(
            "title" to title,
            "completed" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "createdBy" to uid
        )).addOnCompleteListener { result ->
            if (generation != activeGeneration) {
                state = state.copy(saving = false)
                return@addOnCompleteListener
            }
            state = if (result.isSuccessful) {
                state.copy(saving = false, title = "", message = "Task saved to Firebase.")
            } else {
                state.copy(saving = false, message = "Could not create task: ${result.exception?.localizedMessage ?: "Please try again."}")
            }
        }
    }

    fun toggle(task: BoardTask) {
        if (auth.currentUser == null || task.id in state.updating) return
        val activeGeneration = generation
        state = state.copy(updating = state.updating + task.id, message = "")
        // Only completion changes; protected fields remain untouched.
        db.collection("tasks").document(task.id).update("completed", !task.completed)
            .addOnCompleteListener { result ->
                state = state.copy(updating = state.updating - task.id)
                if (generation == activeGeneration && !result.isSuccessful) {
                    state = state.copy(message = "Could not update task: ${result.exception?.localizedMessage ?: "Please try again."}")
                }
            }
    }

    override fun onCleared() {
        auth.removeAuthStateListener(authListener)
        taskListener?.remove()
        generation++
        super.onCleared()
    }
}
