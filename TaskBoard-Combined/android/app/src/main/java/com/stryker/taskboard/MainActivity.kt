package com.stryker.taskboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stryker.taskboard.ui.theme.TaskBoardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskBoardTheme {
                val board: TaskBoardViewModel = viewModel()
                TaskBoardScreen(board.state, board::signIn, board::signOut, board::setTitle,
                    board::addTask, board::toggle, board::listenToTasks)
            }
        }
    }
}

@Composable
fun TaskBoardScreen(
    state: BoardState,
    onSignIn: (String, String) -> Unit,
    onSignOut: () -> Unit,
    onTitleChange: (String) -> Unit,
    onAddTask: () -> Unit,
    onToggle: (BoardTask) -> Unit,
    onRetry: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    // Keep the password only in memory, never in saved instance state.
    var password by remember { mutableStateOf("") }
    LaunchedEffect(state.signedIn) { if (state.signedIn) password = "" }

    Scaffold { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth().imePadding(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Task board", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                        if (state.signedIn) TextButton(onClick = onSignOut) { Text("Log out") }
                    }
                    Text("A simple shared to-do list.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (state.checkingAuth) {
                    item { CircularProgressIndicator(); Text("Loading...") }
                } else if (!state.signedIn) {
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Sign in", style = MaterialTheme.typography.titleLarge)
                                OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(password, { password = it }, label = { Text("Password") }, singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth())
                                Button(onClick = { onSignIn(email, password) }, enabled = !state.signingIn) {
                                    Text(if (state.signingIn) "Signing in..." else "Sign in")
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Text(state.email, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(12.dp))
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(state.title, onTitleChange, label = { Text("New task") },
                                    placeholder = { Text("What needs to get done?") }, singleLine = true,
                                    supportingText = { Text("1–80 characters after trimming whitespace.") },
                                    enabled = !state.saving, modifier = Modifier.fillMaxWidth())
                                Button(onClick = onAddTask, enabled = !state.saving) {
                                    Text(if (state.saving) "Saving..." else "Add task")
                                }
                            }
                        }
                    }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Tasks", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                            if (!state.loadingTasks && state.tasksError.isEmpty()) {
                                Text("${state.tasks.count { it.completed }} / ${state.tasks.size} done")
                            }
                        }
                    }
                    when {
                        state.loadingTasks -> item { CircularProgressIndicator(); Text("Loading tasks...") }
                        state.tasksError.isNotEmpty() -> item {
                            Text(state.tasksError, color = MaterialTheme.colorScheme.error)
                            TextButton(onClick = onRetry) { Text("Retry") }
                        }
                        state.tasks.isEmpty() -> item { Text("No tasks yet. Add one above.") }
                        else -> items(state.tasks, key = { it.id }) { task ->
                            Card(Modifier.fillMaxWidth()) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = task.completed, onCheckedChange = { onToggle(task) }, enabled = task.id !in state.updating)
                                    Text(task.title, modifier = Modifier.weight(1f),
                                        textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                                        color = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
                                    if (task.id in state.updating) CircularProgressIndicator(Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
                if (state.message.isNotEmpty()) item { Text(state.message, style = MaterialTheme.typography.bodyMedium) }
            }
        }
    }
}

// Previews use sample state and never connect to Firebase.
@Preview(showBackground = true)
@Composable
fun TaskBoardPreview() {
    TaskBoardTheme {
        TaskBoardScreen(
            state = BoardState(checkingAuth = false, signedIn = true, email = "tester@example.com",
                tasks = listOf(BoardTask("1", "Try the shared task board", false), BoardTask("2", "Connect Firebase", true))),
            onSignIn = { _, _ -> }, onSignOut = {}, onTitleChange = {}, onAddTask = {}, onToggle = {}, onRetry = {}
        )
    }
}
