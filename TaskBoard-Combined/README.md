# TaskBoard

A simple shared task board with a Next.js/React web client, SwiftUI iOS client, and Jetpack Compose Android client. Firebase Authentication handles email/password sign-in, and Cloud Firestore stores the shared tasks. No external application server is used.

## Repository layout

| Folder | Contents |
| --- | --- |
| `web/` | Next.js/React client |
| `ios/` | SwiftUI app and `TaskBoard.xcodeproj` |
| `android/` | Jetpack Compose app and Gradle wrapper |
| `TEST_RESULTS.md` | Verified checks and outstanding assignment tests |

Each client supports signing in/out, a live task list sorted newest first, adding a title, checking/unchecking tasks, and loading, empty, saving, and error states.

## Firebase setup — required for all clients

Use **one Firebase project for all three apps**. In Firebase Console:

1. Register a Web app, an Apple app, and an Android app in that same project.
2. Enable **Authentication → Sign-in method → Email/Password**.
3. Under **Authentication → Users**, add a test user. Use the same account on each client if desired.
4. Create a Cloud Firestore database. All clients use the top-level collection `tasks`.
5. Deploy secure Firestore rules before testing. They must require authentication, validate all required fields on creation, permit only Boolean `completed` updates, and deny deletion/protected-field edits. This repository does not include or deploy your Firebase Console rules; export and review them for the final submission. Do not enable test mode.

The document fields are `title` (trimmed string, 1–80 characters), `completed` (new tasks use false), `createdAt` (server timestamp), and `createdBy` (signed-in Firebase Auth UID). All clients use live listeners to receive changes from other clients. Documents missing `createdAt` are excluded by the ordered query.

Configuration files are excluded from Git. Download them from your Firebase project and put them in the locations below. Do not paste passwords or Firebase configuration secrets into AI tools. Share the reviewer ENV/configuration through your instructor's approved private channel; never commit it to this public repository.

## Run the web client

Prerequisites: Node.js 20.9 or newer and npm.

```sh
cd web
npm ci
cp .env.example .env.local
```

Fill the four variables in `.env.local` using Firebase Console → Project settings → Your Web app → SDK configuration. Use the `apiKey`, `authDomain`, `projectId`, and `appId` values from that same project.

```sh
npm run dev
```

Open http://localhost:3000 and sign in with the Firebase test user. If port 3000 is occupied, use the address printed by Next.js. Restart the server after changing environment values.

Checks:

```sh
npx next typegen
npx tsc --noEmit
npm run lint
npm run build
```

The Next.js development server serves the web UI; Firebase remains the backend for authentication and task data.

## Run the iOS client

Prerequisites: macOS, Xcode 27, and an installed iPhone simulator runtime matching the project's iOS 27 deployment target.

1. Open `ios/TaskBoard.xcodeproj` in Xcode and allow Swift packages to resolve.
2. Register/select the Apple app with bundle ID **`com.Stryker.TaskBoard`** in your Firebase project.
3. Download `GoogleService-Info.plist` into `ios/GoogleService-Info.plist`. The Xcode project already references that path as an app resource.
4. Select the **TaskBoard** scheme and an **iPhone simulator**, then press Run.
5. Sign in with the Firebase test user.

Canvas is a visual preview with sign-in disabled; use Run to test Firebase. If you select My Mac, choose your development Team under Signing & Capabilities and use a signed build with Keychain Sharing. Firebase Auth error 17995 on Mac indicates a Keychain problem. For physical iPhone testing, also select your development Team and compatible device.

## Run the Android client

Prerequisites: Android Studio, Android SDK 37, and an Android emulator/device with API 24 or newer. Gradle's existing JVM toolchain configuration requests Java 25; allow Android Studio/Gradle to provision it or install it locally. The project uses Gradle 9.6.0, Android Gradle Plugin 9.4.1, and Kotlin/Compose compiler 2.4.0.

1. Open `android/` in Android Studio and let Gradle sync finish.
2. Register/select an Android Firebase app with application ID **`com.example.myapplication`**. This ID matches the current Firebase registration. The source namespace is **`com.stryker.taskboard`**; namespace and installed app ID can differ.
3. Download `google-services.json` into `android/app/google-services.json`.
4. Android Studio generates `local.properties` with your SDK path. Do not commit it.
5. Select the **app** run configuration and an emulator/device, then press Run.
6. Enable the emulator's Wi-Fi and verify it has internet access before signing in.

To change the installed ID to `com.stryker.taskboard`, first register that exact Android package in the same Firebase project and download its configuration; then update `applicationId` and the instrumented-test package assertion. Do not edit Firebase registration values in the JSON by hand.

Command-line checks from `android/`:

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

The debug APK is generated at `android/app/build/outputs/apk/debug/app-debug.apk`.

## Quick shared-data check

Run two clients and sign in. Add a task in one client; it should appear in the other without refreshing. Check it off, then verify the completion change in the first client. Repeat with all three clients and record the eight required cases in `TEST_RESULTS.md`.

## Troubleshooting

- **Network error on Android:** enable emulator Wi-Fi. During setup, Wi-Fi was off while Airplane mode was on; enabling Wi-Fi restored login.
- **Run disabled after Android changes:** Sync Project with Gradle Files and wait for analysis to finish.
- **Android System UI not responding:** this is a virtual-device issue observed during setup. Stop it and try Cold Boot from Device Manager; preserve app data unless you intentionally choose to wipe it.
- **Firestore permission denied:** review the deployed rules and verify the user is authenticated. Do not loosen rules to public access.
- **Web Firebase not configured:** check the four `.env.local` variable names and restart the dev server.
- **iOS build requires signing certificate:** verify that an iPhone simulator is selected instead of My Mac, or configure development signing for Mac.

## Submission checklist

- Share this repository and Firebase project access with the instructor.
- Supply reviewer configuration privately using the three paths documented above.
- Complete all eight required tests and record actual results/evidence.
- Record a 2–3 minute demo creating a task in one client and updating it in another.
- Review and understand all AI-assisted code before submitting.

## AI Use Log

| Tool | Assistance requested | Used or changed |
| --- | --- | --- |
| OpenAI Codex | Explain assignment and investigate web TypeScript/Firebase sign-in errors | Added state types, safe error handling, Firebase exports, and error messages; verified TypeScript and ESLint. |
| OpenAI Codex | Add web task creation, live updates, simple UI, and completion checkboxes | Used Auth UID and server timestamps for creates; live Firestore listener and completion-only updates. |
| OpenAI Codex | Add equivalent iOS features and fix Canvas/build/login issues | SwiftUI UI and Firebase store, consolidated app entry point, linked Firebase products, restored preview, added Mac networking/Keychain capability. |
| OpenAI Codex | Add equivalent Android features and check Firebase setup | Corrected Gradle placement, added Auth/Firestore, aligned Kotlin with Firebase, implemented Compose UI/store and title-validation tests. |
| OpenAI Codex | Make Android source names project-specific and troubleshoot emulator/login | Renamed source namespace/theme/project; retained Firebase-registered app ID; synced Android Studio, cold-booted emulator, and enabled Wi-Fi. |
| OpenAI Codex | Combine the three projects into a public GitHub repository with setup instructions | Organized clients into folders, excluded local configuration/build artifacts, changed the repository's web Firebase setup to environment variables, and wrote setup/test documentation. |

See each client's README for additional implementation notes. Compile/build checks do not replace live cross-client or security-rule tests.
