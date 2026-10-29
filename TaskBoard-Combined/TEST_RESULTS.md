# Test Results

## Checks observed during development

| Check | Result |
| --- | --- |
| Web TypeScript and ESLint | Passed in the combined web folder after generating Next.js route types (`next typegen`). |
| iOS simulator build, arm64 and x86_64 | Passed with signing disabled for compilation checks. |
| iOS Xcode Run | TaskBoard opened on iPhone 17e simulator. Live iOS login was not verified by the agent. |
| Android debug build | Passed; APK generated. |
| Android unit tests | Four passed: three title-validation tests plus existing template test. |
| Android lint | No errors; dependency/template warnings remain. |
| Android instrumented-test APK | Compiled; not executed. |
| Android live sign-in and task reads | Observed signed-in task list after enabling emulator Wi-Fi. |
| Firebase configuration consistency | Android package matches its JSON registration; iOS bundle ID matches its plist; project IDs matched across clients. Values were not published. |

## Required assignment tests

Do not mark these passed until the actual actions and cross-client results have been observed and recorded.

| # | Test | Result | Evidence/date |
| --- | --- | --- | --- |
| 1 | Add from web; verify in iOS and Android | Pending | |
| 2 | Add from iOS; verify in web and Android | Pending | |
| 3 | Add from Android; verify in web and iOS | Pending | |
| 4 | Toggle completion in one client; verify in the other two | Pending | |
| 5 | Reject blank/whitespace-only title | Android validation unit test passed; live checks on all clients pending | |
| 6 | Reject title longer than 80 characters | Android validation unit test passed; live checks on all clients pending | |
| 7 | Deny signed-out reads and writes | Pending server-side security-rule test | |
| 8 | Deny deletion and changes to title, createdAt, createdBy | Pending server-side security-rule test | |

Use Firebase's Rules Playground or Emulator Suite for denied-operation tests. Merely hiding UI controls does not verify server-side security. No deployed rules or reviewer environment secrets are included in this public repository.
