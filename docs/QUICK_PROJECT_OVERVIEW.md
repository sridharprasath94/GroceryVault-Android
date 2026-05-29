# GroceryVault — Quick Project Overview

## What the app does
Grocery list manager with Firebase Firestore sync. Users can create, edit, and check off grocery lists. Data syncs across devices in real-time and also on-demand.

---

## Package layout

```
com.flash.groceryVault/
├── data/                    # Room entities, DAOs, GroceryRepository, SuggestionsRepository
├── firebase/                # FirestoreSyncService, FirebaseBackupService, FirebaseImageStorage
├── di/                      # AppContainer (manual service locator, per-user caching)
├── worker/                  # PeriodicFirebaseBackupWorker (WorkManager)
├── ui/
│   ├── screens/
│   │   ├── auth/            # AuthScreen + AuthViewModel
│   │   ├── listGrocery/     # GroceryListScreen + GroceryListViewModel  ← most complex
│   │   ├── createGrocery/   # CreateGroceryScreen + CreateGroceryViewModel
│   │   ├── detailGrocery/   # GroceryDetailScreen + GroceryDetailViewModel
│   │   └── editGrocery/     # EditGroceryScreen + EditGroceryViewModel
│   ├── components/          # 10 reusable Compose components (see REUSABLE_COMPONENTS.md)
│   ├── data/                # UI-layer data classes (GroceryListItem)
│   ├── theme/               # Color.kt, Theme.kt, Type.kt
│   └── util/                # DateFormats, ImageUtil, SimpleJson
└── MainActivity.kt / AppRoot.kt
```

---

## Navigation (AppRoot.kt)

```
NavHost
├── "auth"          → AuthScreen
├── "list"          → GroceryListScreen        (start if logged in)
├── "create"        → CreateGroceryScreen
├── "detail/{id}"   → GroceryDetailScreen      (Long arg)
└── "edit/{id}"     → EditGroceryScreen        (Long arg)
```

Auth state drives `startDestination`: logged-in → list, logged-out → auth.

---

## Data flow

```
Firebase Firestore  ←→  FirestoreSyncService  ←→  GroceryRepository
                                                           ↓
                                                     Room Database
                                                     (grocery_db_<uid>)
                                                           ↓
                                                    ViewModel (StateFlow)
                                                           ↓
                                                    Compose UI
```

- **Real-time**: `FirestoreSyncService.startRealTime()` runs a Firestore snapshot listener; wired in `AppRoot.kt`
- **Manual**: `vm.syncNowWithCloud()` in `GroceryListViewModel` → `FirestoreSyncService.syncNow()`
- **Conflict resolution**: Last-write-wins via `updatedAt` timestamp
- **Soft deletes**: `isDeleted = true, deletedAt = timestamp` (never hard-delete locally before sync)
- **Backup**: `FirebaseBackupService` stores full JSON to `users/<uid>/backup/latest`

---

## State management pattern (all ViewModels)

```kotlin
private val _ui = MutableStateFlow(SomeUiState())
val ui: StateFlow<SomeUiState> = _ui.asStateFlow()

private val _events = MutableSharedFlow<SomeEvent>(extraBufferCapacity = 1)
val events: SharedFlow<SomeEvent> = _events.asSharedFlow()
```

Screens collect `ui` with `collectAsState()` and handle `events` with `collectLatest`.

---

## DI — AppContainer

Manual service locator (no Hilt). Caches repos and services per logged-in user:
- `groceryRepositoryForCurrentUser` — lazy, keyed to current UID
- `suggestionsRepository` — lazy
- `firestoreSyncServiceForCurrentUser()` — cached, invalidated on UID change
- `firebaseBackupServiceForCurrentUser()` — cached

Database name: `grocery_db_<uid>` — each user gets their own Room database.

---

## Key dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Compose BOM | 2025.12.01 | All Compose UI |
| Material3 | (BOM) | UI components |
| Navigation Compose | 2.9.6 | Screen routing |
| Room | (rootProject.extra) | Local DB |
| Firebase Auth | 23.2.1 | Auth |
| Firebase Firestore | 25.1.4 | Cloud sync |
| Firebase Storage | 22.0.1 | Image upload |
| Coil | 2.7.0 | Image loading |
| WorkManager | 2.11.0 | Background backup |
| Inter (Google Fonts) | — | App typography |

---

## Theme

- **Font**: Inter (Google Fonts), applied to all Material3 typography slots
- **Colors**: Material3 green/nature palette — primary `#4C662B` (light) / `#B1D18A` (dark)
- **Dynamic color**: Disabled (`dynamicColor = false` in `GroceryVaultTheme`)
- **Dark mode**: Full support via `DarkColors` in Theme.kt

---

## Screen complexity at a glance

| Screen | Lines | Risk | Notes |
|--------|-------|------|-------|
| GroceryListScreen | 431 | HIGH | Sync state machine, SharedPreferences, GoogleSignIn |
| AuthScreen | 195 | HIGH | FirebaseAuth + GoogleSignIn activity result |
| GroceryDetailScreen | 213 | LOW | Read + checkbox toggle only |
| CreateGroceryScreen | 147 | LOW | Form insert, no conflict resolution |
| EditGroceryScreen | 149 | LOW | Form update, no conflict resolution |
