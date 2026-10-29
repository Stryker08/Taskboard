This is a [Next.js](https://nextjs.org) project bootstrapped with [`create-next-app`](https://nextjs.org/docs/app/api-reference/cli/create-next-app).

## Getting Started

First, run the development server:

```bash
npm run dev
# or
yarn dev
# or
pnpm dev
# or
bun dev
```

Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.

You can start editing the page by modifying `app/page.tsx`. The page auto-updates as you edit the file.

This project uses [`next/font`](https://nextjs.org/docs/app/building-your-application/optimizing/fonts) to automatically optimize and load [Geist](https://vercel.com/font), a new font family for Vercel.

## Learn More

To learn more about Next.js, take a look at the following resources:

- [Next.js Documentation](https://nextjs.org/docs) - learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) - an interactive Next.js tutorial.

You can check out [the Next.js GitHub repository](https://github.com/vercel/next.js) - your feedback and contributions are welcome!

## Deploy on Vercel

The easiest way to deploy your Next.js app is to use the [Vercel Platform](https://vercel.com/new?utm_medium=default-template&filter=next.js&utm_source=create-next-app&utm_campaign=create-next-app-readme) from the creators of Next.js.

Check out our [Next.js deployment documentation](https://nextjs.org/docs/app/building-your-application/deploying) for more details.

## AI Use Log

- Tool: OpenAI Codex. Asked to fix errors in `app/page.tsx`. Added explicit user/task types, validated task data, narrowed caught errors, handled logout failures, and added environment-based Firebase initialization with a missing-configuration message. TypeScript and ESLint checks were run; live Firebase login and reads still require configuration and manual testing. Review and understand these changes before submission.

- Tool: OpenAI Codex. Asked to fix page errors after updating Firebase setup. Added missing `auth` and `db` exports, reused the initialized Firebase app, and removed unused browser-only Analytics initialization. Preserved existing configuration. Verified with TypeScript and ESLint; live Firebase access remains untested.

- Tool: OpenAI Codex. Investigated login rejection at `await`. Updated login to show Firebase error codes and actionable messages, trim email whitespace, reject empty credentials, and show sign-in progress. TypeScript and ESLint checked; successful live sign-in still requires manual verification.

## Shared task creation

Sign in, enter a title, and choose Add task. The web client trims the title, checks that it contains 1–80 characters, and writes to Firestore `tasks` with `completed: false`, `createdAt: serverTimestamp()`, and `createdBy` set to the current Auth UID. A live subscription displays tasks newest first and receives changes from other clients.

The iOS and Android apps must use the same Firebase project and collection, authenticate with Firebase Auth, and read the same fields. Use a Firestore snapshot listener in each mobile app for automatic updates. Tasks created manually without `createdAt` will not appear in the ordered query. Your deployed Firestore rules must allow authenticated reads and validated creates; no rules were deployed by this change.

Manual verification still required: log in, add a valid title, confirm the document's four fields in Firestore, and confirm it appears in both mobile clients. Try whitespace-only and 81-character titles and confirm neither creates a document. Check signed-out access and protected-field rules separately. Cross-platform results have not been verified.

- Tool: OpenAI Codex. Asked to enable web task creation and sharing with other apps. Added validated Firestore creates, a live shared task listener, loading/error/empty states, and retry. TypeScript and ESLint checked. Review the addDoc/serverTimestamp/onSnapshot flow and complete the manual cross-client checks before submission.

- Tool: OpenAI Codex. Asked for simple UI styling and task checkboxes. Added a responsive card layout and labeled completion checkboxes that update only `completed` in Firestore, disable during saves, and report failures. TypeScript and ESLint checked; verify checking and unchecking in two clients with deployed completion-update rules.
