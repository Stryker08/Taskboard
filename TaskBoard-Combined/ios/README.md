# iOS Task Board

Open TaskBoard.xcodeproj in Xcode, allow Swift packages to resolve, select the TaskBoard scheme and an iPhone simulator, then Run. For a physical device, select your development team under Signing & Capabilities.

FirebaseCore, FirebaseAuth, and FirebaseFirestore are linked from the existing Firebase package. Keep GoogleService-Info.plist in the app resource bundle. It must belong to the same Firebase project as the web app, and its iOS app registration must match the bundle identifier. Do not share configuration values or passwords with AI tools. Enable Email/Password Authentication and sign in using an existing Firebase test user. Use authenticated, validated Firestore rules; do not enable test mode.

## Features

- Email/password sign-in and sign-out.
- Live tasks from the shared `tasks` collection, newest first.
- Add a title containing 1–80 Unicode scalars after trimming whitespace.
- New documents contain title, completed: false, createdAt: FieldValue.serverTimestamp(), and createdBy: the signed-in Auth UID.
- Check or uncheck a task; only completed is updated.
- Loading, empty, error, retry, and saving states.

TaskBoardApp.swift configures Firebase once. TaskBoardStore.swift handles auth, validation, writes, and a live Firestore listener. ContentView.swift provides the SwiftUI interface. Listeners detach when the view disappears or the user changes.

## Manual checks (pending)

1. Add from web and verify in iOS and Android.
2. Add from iOS and verify in web and Android.
3. Add from Android and verify in web and iOS.
4. Check and uncheck tasks and verify the change in other clients.
5. Reject blank/whitespace-only titles.
6. Reject titles longer than 80 characters.
7. Verify unauthenticated reads/writes are denied by Firestore rules.
8. Verify deletion and protected-field edits are denied by Firestore rules.

No live cross-client or security-rule results have been recorded yet. UI sign-out alone does not prove server-side authorization.

## AI Use Log

- Tool: OpenAI Codex. Request: add the web app's simple UI, Firebase login, task creation, live list, and completion toggles to iOS. Added TaskBoardStore and the SwiftUI interface, consolidated duplicate app entry points, and linked Firebase products in the existing Xcode project. Review and explain the listener, server timestamp, UID, and completion-only write flow before submission. Build results are reported in the chat; manual cross-client tests remain pending.

## Verification

- October 28, 2026: Xcode 27 simulator build passed for arm64 and x86_64 with code signing disabled.
- Xcode project syntax validation and Swift syntax parsing passed.
- Locally compared configuration: iOS and web Firebase project IDs match, and the iOS plist bundle ID matches the Xcode target. No configuration values were printed.
- Live login, Firestore writes, cross-client synchronization, and security-rule tests are not yet verified.

Firebase API references: https://firebase.google.com/docs/auth/ios/password-auth and https://firebase.google.com/docs/firestore/manage-data/add-data

- Tool: OpenAI Codex. Investigated missing Xcode Canvas. Restored the ContentView #Preview declaration and added a preview mode that displays sign-in without starting Firebase listeners.

- Tool: OpenAI Codex. Investigated ContentView line 125. Changed the logout toolbar placement from iOS-specific topBarTrailing to automatic for compatibility with the project's Mac preview destination.

- Tool: OpenAI Codex. Investigated failing sign-in. Added Firebase Auth numeric error codes, labeled Canvas as a visual preview with login disabled, and added a Mac-only outgoing-network entitlement while retaining App Sandbox. Verify real sign-in using Run and an existing Firebase test user; credentials were not inspected.

## Mac sign-in and Keychain

Firebase Auth error 17995 means Keychain access failed. The Mac target includes Keychain Sharing scoped to this app's identifier. In Xcode, select TaskBoard target > Signing & Capabilities, select your Apple development team, keep automatic signing enabled, and verify Keychain Sharing is present. Run the signed app using Xcode. An unsigned build only verifies compilation and cannot validate Mac Keychain access. For iOS testing, select an iPhone simulator and Run.

Reference: https://firebase.google.com/docs/ios/troubleshooting-faq#macos-keychain-sharing

- Tool: OpenAI Codex. Diagnosed Firebase Auth 17995 from a screenshot. Added a Mac Keychain access group scoped to this app, enabled its Xcode capability, and exposed localized failure reasons without logging credentials. Team selection and signed runtime login still require user verification.
