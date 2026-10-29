# Android Task Board

Open this folder in Android Studio, allow Gradle sync to finish, select the app run configuration and an Android emulator (API 24 or newer), then Run. The project compiles with the installed Android 37 SDK and uses its existing Gradle/Android Gradle Plugin versions. Kotlin and its Compose compiler plugin are aligned to 2.4.0 because Firebase Auth 25 requires Kotlin 2.4 metadata support.

## Firebase setup

The existing app/google-services.json is in the correct app module. Its Android client package matches com.example.myapplication, and its Firebase project matches iOS and web. Configuration values were compared locally without printing them. The Google Services plugin is declared at the root and applied to app; Auth and Firestore use the existing Firebase BoM version. INTERNET permission is declared. Firebase's Android provider initializes the default app from generated resources automatically.

Enable Email/Password in Firebase Authentication and sign in with an existing Firebase test user. All clients use tasks in the same Firebase project. Keep deployed Firestore rules authenticated and validated; no rules were changed or deployed. No external server is used. Do not enter passwords or configuration secrets into AI tools.

## Features

- Email/password sign-in and sign-out.
- Shared live task list sorted by createdAt descending.
- Create a title containing 1–80 Unicode code points after trimming whitespace.
- New tasks include title, completed: false, createdAt: FieldValue.serverTimestamp(), and createdBy: current Auth UID.
- Check/uncheck tasks; only completed is updated.
- Loading, empty, saving, and error/retry states.
- Compose preview uses sample data without Firebase.

TaskBoardViewModel manages auth, Firestore listeners, and writes. Its lifecycle survives rotation and removes listeners when cleared. MainActivity provides the simple Compose UI. TaskValidation contains title validation and boundary tests.

## Verification

Run ./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug. Verified October 28, 2026:

- Debug APK build passed: app/build/outputs/apk/debug/app-debug.apk.
- Four unit tests passed with no failures: three title-validation tests plus the existing template test.
- Android lint passed with no errors. Remaining warnings concern dependency updates, version-catalog placement, and unused/template resources.
- Google Services resource generation passed, confirming the app registration is recognized by the plugin.
- Live login, task writes, cross-platform sync, and Firestore security-rule results remain pending.

The eight required manual tests remain pending: create from each of web/iOS/Android and verify the other clients; toggle completion across clients; reject whitespace-only titles; reject titles over 80 characters; verify signed-out reads/writes are denied; verify deletion and protected-field edits are denied. UI sign-out alone does not verify Firestore security.

## AI Use Log

- Tool: OpenAI Codex. Request: add the web/iOS task-board features to Android and check Firebase setup. Fixed root-versus-app Gradle plugin/dependency placement, added Auth/Firestore and INTERNET permission, and implemented a Compose UI with live tasks, validation, creates, completion toggles, and error states. Added validation tests and a Firebase-free preview. Review and explain the listener, server timestamp, UID, and completion-only update before submission. Live login and shared-data/security-rule tests require manual verification.

Firebase setup reference: https://firebase.google.com/docs/android/setup

## Android package names

The project's source namespace is `com.stryker.taskboard`, its Compose theme is TaskBoardTheme, and its Gradle project name is TaskBoard. Main, unit-test, and instrumented-test files use the same source namespace, with matching directory paths and imports. The manifest explicitly launches com.stryker.taskboard.MainActivity.

The installed application ID remains `com.example.myapplication` because that is the Android app registered in the existing google-services.json. This is a template identifier, not a reference to a different Firebase project. Namespace and installed application ID can differ. To change the installed ID to `com.stryker.taskboard`, register that exact Android package in the same Firebase project, download its new google-services.json to app/, and then update applicationId and the instrumented package assertion. Do not edit the registration values in the JSON by hand.

- Tool: OpenAI Codex. Asked to verify project-specific packages. Audited Gradle namespace/applicationId, all Kotlin packages/imports, the manifest, theme, tests, and Firebase's registered Android package. Renamed source namespace, directories, theme, and Gradle project to TaskBoard; preserved Firebase's installed application ID to keep the existing configuration valid pending a new Firebase registration.
