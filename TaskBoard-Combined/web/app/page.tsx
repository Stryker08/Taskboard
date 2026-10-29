"use client";

import { useEffect, useState } from "react";
import { FirebaseError } from "firebase/app";
import type { FormEvent } from "react";
import type { User } from "firebase/auth";
import {
  signInWithEmailAndPassword,
  signOut,
  onAuthStateChanged
} from "firebase/auth";

import {
  collection,
  doc,
  updateDoc,
  addDoc,
  onSnapshot,
  serverTimestamp,
  query,
  orderBy
} from "firebase/firestore";

import { auth, db } from "../lib/firebase";

type Task = {
  id: string;
  title: string;
  completed: boolean;
};

export default function Home() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [signingIn, setSigningIn] = useState(false);
  const [user, setUser] = useState<User | null>(null);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [title, setTitle] = useState("");
  const [savingTask, setSavingTask] = useState(false);
  const [updatingTasks, setUpdatingTasks] = useState<string[]>([]);
  const [tasksLoading, setTasksLoading] = useState(false);
  const [tasksError, setTasksError] = useState("");
  const [subscriptionVersion, setSubscriptionVersion] = useState(0);
  const [message, setMessage] = useState(auth ? "" : "Firebase is not configured. Add your Firebase web configuration to .env.local and restart the dev server.");
  const [loading, setLoading] = useState(Boolean(auth));

  // Check if the user is logged in
  useEffect(() => {
    if (!auth) return;

    let stopTasks: (() => void) | undefined;
    const unsubscribe = onAuthStateChanged(auth, (currentUser) => {
      stopTasks?.();
      stopTasks = undefined;
      setUser(currentUser);
      setTasks([]);
      setTasksError("");
      setLoading(false);
      setTasksLoading(Boolean(currentUser));
      if (!currentUser || !db) return;

      // Listen to the shared collection, including changes from mobile clients.
      stopTasks = onSnapshot(
        query(collection(db, "tasks"), orderBy("createdAt", "desc")),
        (snapshot) => {
          try {
            const taskList: Task[] = snapshot.docs.map((document) => {
              const data = document.data();
              if (typeof data.title !== "string" || typeof data.completed !== "boolean") {
                throw new Error("A task has invalid fields.");
              }
              return { id: document.id, title: data.title, completed: data.completed };
            });
            setTasks(taskList);
            setTasksError("");
          } catch (error) {
            setTasksError(error instanceof Error ? error.message : "Invalid task data.");
          }
          setTasksLoading(false);
        },
        (error) => {
          setTasksError(`Failed to load tasks (${error.code}). Check your connection and Firestore permissions.`);
          setTasksLoading(false);
        }
      );
    });

    return () => {
      unsubscribe();
      stopTasks?.();
    };
  }, [subscriptionVersion]);

  // Login to Firebase
  async function handleLogin() {
    if (!auth) return;
    if (!email.trim() || !password) {
      setMessage("Enter your email and password.");
      return;
    }
    setMessage("");
    setSigningIn(true);

    try {
      await signInWithEmailAndPassword(auth, email.trim(), password);
      setMessage("Successfully logged in!");
    } catch (error) {
      const code = error instanceof FirebaseError ? error.code : "unknown";
      const reasons: Record<string, string> = {
        "auth/invalid-credential": "Check the email and password. The user must exist in this Firebase project's Authentication Users list.",
        "auth/user-not-found": "This Firebase project has no user with that email.",
        "auth/wrong-password": "The password is incorrect.",
        "auth/invalid-email": "Enter a valid email address.",
        "auth/operation-not-allowed": "Enable Email/Password in Firebase Authentication's Sign-in method settings.",
        "auth/configuration-not-found": "Set up Authentication and enable Email/Password in this Firebase project.",
        "auth/invalid-api-key": "Check the Firebase API key in your app configuration.",
        "auth/user-disabled": "This account is disabled in Firebase Authentication.",
        "auth/too-many-requests": "Too many login attempts. Wait before trying again.",
        "auth/network-request-failed": "Check your internet connection and try again.",
      };
      setMessage(`Login failed (${code}). ${reasons[code] ?? "Check your Firebase project configuration and try again."}`);
    } finally {
      setSigningIn(false);
    }
  }

  // Write the same fields expected by the iOS and Android clients.
  async function handleAddTask(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (savingTask) return;
    const currentUser = auth?.currentUser;
    if (!currentUser || !db) {
      setMessage("Sign in before adding a task.");
      return;
    }
    const trimmedTitle = title.trim();
    if (Array.from(trimmedTitle).length < 1 || Array.from(trimmedTitle).length > 80) {
      setMessage("Task titles must contain 1–80 characters after trimming whitespace.");
      return;
    }
    setSavingTask(true);
    setMessage("");
    try {
      await addDoc(collection(db, "tasks"), {
        title: trimmedTitle,
        completed: false,
        createdAt: serverTimestamp(),
        createdBy: currentUser.uid,
      });
      setTitle("");
      setMessage("Task created and saved to Firebase.");
    } catch (error) {
      const code = error instanceof FirebaseError ? error.code : "unknown";
      setMessage(`Could not create task (${code}). Check your connection and Firestore permissions.`);
    } finally {
      setSavingTask(false);
    }
  }

  async function handleToggleTask(task: Task) {
    if (!auth?.currentUser || !db || updatingTasks.includes(task.id)) return;
    setUpdatingTasks((ids) => [...ids, task.id]);
    setMessage("");
    try {
      // Only completion changes; all other document fields stay protected.
      await updateDoc(doc(db, "tasks", task.id), { completed: !task.completed });
      setMessage(task.completed ? "Task marked incomplete." : "Task completed.");
    } catch (error) {
      const code = error instanceof FirebaseError ? error.code : "unknown";
      setMessage(`Could not update task (${code}). Check your connection and Firestore permissions.`);
    } finally {
      setUpdatingTasks((ids) => ids.filter((id) => id !== task.id));
    }
  }

  // Logout
  async function handleLogout() {
    if (!auth) return;
    try {
      await signOut(auth);
      setTasks([]);
      setMessage("Logged out.");
    } catch (error) {
      console.error(error);
      setMessage("Logout failed. Please try again.");
    }
  }

  return (
    <main className="board">
      <header className="board-header">
        <div>
          <h1>Task board</h1>
          <p className="muted">A simple shared to-do list.</p>
        </div>
        {user && <button className="secondary" onClick={handleLogout}>Log out</button>}
      </header>

      {loading && <p role="status">Loading...</p>}
      {!loading && !user && (
        <form className="login-form" onSubmit={(event) => { event.preventDefault(); void handleLogin(); }}>
          <h2>Sign in</h2>
          <label htmlFor="email">Email</label>
          <input id="email" type="email" autoComplete="username" required value={email} onChange={(event) => setEmail(event.target.value)} />
          <label htmlFor="password">Password</label>
          <input id="password" type="password" autoComplete="current-password" required value={password} onChange={(event) => setPassword(event.target.value)} />
          <button disabled={signingIn}>{signingIn ? "Signing in..." : "Sign in"}</button>
        </form>
      )}

      {!loading && user && (
        <section>
          <p className="muted signed-in">{user.email ?? "Signed in"}</p>
          <form onSubmit={handleAddTask}>
            <label htmlFor="task-title">New task</label>
            <div className="add-row">
              <input id="task-title" value={title} onChange={(event) => setTitle(event.target.value)} placeholder="What needs to get done?" disabled={savingTask} aria-describedby="title-help" />
              <button type="submit" disabled={savingTask}>{savingTask ? "Saving..." : "Add task"}</button>
            </div>
            <p id="title-help" className="muted help">1–80 characters.</p>
          </form>

          <div className="list-heading">
            <h2>Tasks</h2>
            {!tasksLoading && !tasksError && <span className="muted">{tasks.filter((task) => task.completed).length} / {tasks.length} done</span>}
          </div>
          {tasksLoading && <p role="status">Loading tasks...</p>}
          {tasksError && <div role="alert"><p>{tasksError}</p><button className="secondary" onClick={() => setSubscriptionVersion((version) => version + 1)}>Retry</button></div>}
          {!tasksLoading && !tasksError && tasks.length === 0 && <p className="empty">No tasks yet. Add one above.</p>}
          {!tasksError && tasks.length > 0 && (
            <ul className="task-list">
              {tasks.map((task) => (
                <li key={task.id}>
                  <label className={`task-row${task.completed ? " completed" : ""}`}>
                    <input type="checkbox" checked={task.completed} disabled={updatingTasks.includes(task.id)} onChange={() => void handleToggleTask(task)} />
                    <span>{task.title}</span>
                    {updatingTasks.includes(task.id) && <small className="muted">Saving...</small>}
                  </label>
                </li>
              ))}
            </ul>
          )}
        </section>
      )}
      {message && <p className="message" role="status">{message}</p>}
    </main>
  );
}
