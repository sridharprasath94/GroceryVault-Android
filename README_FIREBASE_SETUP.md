# Firebase Setup (GrocerySaver)

1) Create a Firebase project in the Firebase Console.
2) Add an Android app with package name: **com.flash.grocerys**
3) Download `google-services.json` and replace:
   `app/google-services.json` (this repo ships with a dummy file)

4) In Firebase Console:
   - Authentication -> Sign-in method -> enable **Email/Password**
   - Firestore Database -> Create database (in production/test mode)

## What this app does
- Login screen (email/password)
- Per-user local Room database named: `grocery_db_<uid>`
- Grocery supports optional image URI (picked from gallery)
- Backup:
  - **Cloud** button uploads latest JSON to Firestore: `users/<uid>/backups/latest`
  - Periodic backup runs every 6 hours via WorkManager (only uploads when logged in)
  - **Backup** button saves JSON using system file picker (choose Google Drive to store in Drive)
  - **Share** button shares JSON via Android share sheet (you can pick Google Drive, Gmail, etc.)
