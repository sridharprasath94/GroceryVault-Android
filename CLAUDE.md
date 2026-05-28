# CLAUDE.md — GroceryVault Engineering Rules

> Engineering guide for all Claude Code sessions on this project.
> Read this before touching any code. These rules exist because violations here
> break Firebase sync, corrupt user data, or cause silent build failures.

---

## Project Identity

**App**: GroceryVault — grocery list manager with Firebase Firestore sync  
**Package**: `com.flash.groceryVault`  
**Stack**: Kotlin · Jetpack Compose · Material3 · MVVM · Room · Firebase  
**Min SDK**: 24 · **Target SDK**: 35  
**No XML. No View Binding. No Hilt. No Clean Architecture layers.**

Full overview: `docs/QUICK_PROJECT_OVERVIEW.md`  
Risk map: `docs/SAFE_UI_MODIFICATION_GUIDE.md`  
Component catalogue: `docs/REUSABLE_COMPONENTS.md`

---

## Zone Map — Read Before Every Change

### 🚫 NEVER TOUCH

These files contain the sync state machine and conflict resolution logic. Any change risks data loss, duplicate syncs, or silent corruption.

| File | Why |
|------|-----|
| `firebase/FirestoreSyncService.kt` | Two-way sync algorithm, last-write-wins logic |
| `ui/screens/listGrocery/GroceryListViewModel.kt` | Sync orchestration, `didAutoSync` guard, navigation race prevention |
| `GroceryListScreen.kt` — all four `LaunchedEffect` blocks | Ordered sync state machine; reordering causes stale badges or infinite sync loops |
| `GroceryRepository.kt` — `SyncOrigin` emission | Drives the cloud-sync status indicator across the app |
| `di/AppContainer.kt` | Per-user DB caching; touching this breaks user isolation |

**Specific things never to rename or remove:**
```kotlin
// GroceryListUiState fields — all are load-bearing for sync UI
isSyncing, isCloudSynced, lastSyncedAt, didAutoSync, isNavigating, isLoadingData

// SharedPreferences keys — changing these loses sync state across restarts
private const val cloudSyncedStatusKey = "cloud_synced"
private const val cloudLastSyncedTimeKey = "cloud_last_synced_at"
```

**Never hard-delete a list locally** — soft-delete only (`isDeleted = true`). Hard deletes break the push-before-pull sync flow.

---

### ⚠️ HIGH RISK — Change with explicit justification

| File | Risk | Notes |
|------|------|-------|
| `AuthScreen.kt` + `AuthViewModel.kt` | Auth entry point, GoogleSignIn activity result | UI changes OK; do not touch the `addAuthStateListener` or `GoogleSignInClient` setup |
| `AppRoot.kt` | Navigation graph + real-time sync lifecycle | `DisposableEffect` that calls `startRealTime`/`stopRealTime` is critical |
| `GroceryListScreen.kt` — `GroceryListContent` | Sync state reads, `isInteractionEnabled` guard | Card/FAB/empty-state sections inside are safe; event handler block is not |
| `GroceryRepository.kt` — mutations | `createList`, `updateList`, `deleteList` must keep emitting `SyncOrigin.Local` | |

For any HIGH RISK change: explain what you're changing and why it's safe before touching the file.

---

### ✅ SAFE — Edit freely

| Area | Notes |
|------|-------|
| `ui/theme/Color.kt` | All design tokens; never hardcoded in screens |
| `ui/theme/Theme.kt` | Color scheme wiring; safe to change `dynamicColor`, dark/light |
| `ui/theme/Type.kt` | Font family, type scale |
| `ui/components/` — all 10 files | Pure presentational, no business logic |
| `GroceryDetailScreen.kt` | Read + toggle display only; ViewModel not coupled to sync |
| `CreateGroceryScreen.kt` | Local form, simple insert |
| `EditGroceryScreen.kt` | Local form, simple update |
| `GroceryListCard` composable | Display-only; callbacks are passed in, not called |
| Empty-state branch in `GroceryListContent` | No ViewModel calls allowed here |

---

## Architecture Rules

- **Do not migrate to Hilt.** `AppContainer` is the intentional DI solution.
- **Do not introduce new architecture layers.** No Use Cases, no domain module, no mappers.
- **Do not add repositories** — `GroceryRepository` and `SuggestionsRepository` cover the full data model.
- **One Activity** — `MainActivity`. All navigation is Compose NavHost.
- **Per-user databases** — Room DB is named `grocery_db_<uid>`. Never share databases across users.
- **State flows one way**: Firebase → Repository → ViewModel (`StateFlow`) → Compose UI. Never reverse this.

---

## Compose Rules

**Always use Material3 tokens — never hardcode colors or sizes:**
```kotlin
// ✅ Good
color = MaterialTheme.colorScheme.onSurfaceVariant
style = MaterialTheme.typography.bodyMedium

// ❌ Bad
color = Color(0xFF586249)
fontSize = 14.sp
```

**Derive display values locally — never push display logic into ViewModels:**
```kotlin
// ✅ Good — computed in the composable
val checkedCount = ui.groceryItems.count { it.isChecked }
val displayItems = ui.groceryItems.sortedBy { it.isChecked }

// ❌ Bad — pollutes ViewModel with presentation logic
data class UiState(val sortedItems: List<...>, val checkedCount: Int)
```

**Use `rememberSaveable` for local UI state that should survive rotation:**
```kotlin
var hidePurchased by rememberSaveable { mutableStateOf(false) }
```

**Use `remember` for derived values that are expensive:**
```kotlin
val displayItems = remember(ui.groceryItems) { ui.groceryItems.sortedBy { it.isChecked } }
```

**Weight vs fillMaxSize in Column/Row:**
```kotlin
// ✅ In a Column, use weight(1f) to fill remaining space
LazyColumn(modifier = Modifier.weight(1f)) { ... }

// ❌ fillMaxSize() in a Column child causes layout conflicts with siblings
LazyColumn(modifier = Modifier.fillMaxSize()) { ... }
```

**New composable parameters must have defaults** — existing call sites must not need updates:
```kotlin
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    newParam: Boolean = false,      // ✅ always add a default
    content: @Composable () -> Unit,
)
```

**`@OptIn(ExperimentalMaterial3Api::class)` is required** for `TopAppBar`, `PullToRefreshBox`, `ExposedDropdownMenuBox`.

---

## State Management Rules

- Every screen has exactly one `UiState` data class and one `Event` sealed interface.
- `UiState` is exposed as `StateFlow`, events as `SharedFlow`.
- Screens collect state with `collectAsState()` and events with `collectLatest` inside a `LaunchedEffect(Unit)`.
- `isNavigating` flag in UiState gates all event emission — never remove it or bypass it.
- `isLoadingData` flag drives the interaction-blocking overlay — never remove it.
- Navigation is triggered only through the event system — composables call `vm::requestX`, which emits an event, which the screen handles.

**Event collection template** (do not deviate):
```kotlin
LaunchedEffect(Unit) {
    vm.events.collectLatest { event ->
        when (event) {
            is SomeEvent.Navigate -> {
                vm.startNavigation()   // must come before the navigation call
                onNavigate()
            }
        }
    }
}
```

---

## ViewModel Rules

- ViewModels receive `AppContainer` (or specific repos) — never `Context` directly.
- Use `_ui.update { it.copy(...) }` for state mutations — never replace the entire state object.
- Use `_events.tryEmit(...)` for fire-and-forget events.
- Use `viewModelScope.launch` for all async work.
- Call `emitIfAllowed()` (checks `isNavigating`) before emitting navigation events.
- **Never add ViewModel methods for pure display logic** — sorting, filtering, counting belong in the composable.
- **Never expose raw Room `Flow`s from ViewModel** — always map to `UiState`.

---

## Firebase / Sync Safety Rules

- **`syncNow()` is the only correct sync entry point.** Don't call repository methods directly to "manually sync."
- **Real-time listener lifecycle** is managed in `AppRoot.kt` `DisposableEffect`. Don't start or stop it from screens.
- **`updatedAt` timestamps are the conflict resolution key.** Any mutation must update `updatedAt` — the repository handles this.
- **`SyncOrigin.Local` must be emitted** after every local mutation (create/update/delete). The ViewModel subscribes to this to set `isCloudSynced = false`.
- **`applyRemoteList()` is the only correct way to write remote data into Room.** Don't call `insertList()` or `updateList()` with remote data directly.
- **Firestore document structure is stable** — fields: `id`, `title`, `description`, `isDeleted`, `deletedAt`, `createdAt`, `updatedAt`, `items[]`. Renaming any field breaks sync for existing users.

---

## Build Rules

After **any** code change, validate the build:
```bash
./gradlew clean :app:assembleDebug --no-build-cache
```

Run tests when changing data/repository/ViewModel logic:
```bash
./gradlew :app:testDebugUnitTest
```

Run lint before any PR:
```bash
./gradlew :app:lintDebug
```

**Never leave the project in a non-compiling state.** Fix all of:
- Unresolved imports
- Type mismatches
- Compose compiler errors
- Navigation argument type errors
- Preview errors (they break the build)

**Never skip `clean` or `--no-build-cache`** — the Gradle/Compose cache produces stale outputs that mask real errors.

---

## Gradle Rules

- All dependencies must be declared in `libs.versions.toml` — never hardcode versions in `build.gradle.kts`.
- Do **not** add `alias(libs.plugins.kotlin.android)` to `app/build.gradle.kts` — AGP 9.x bundles Kotlin.
- Required Gradle wrapper: `gradle-9.4.1-bin.zip`
- Required: `agp = "9.2.1"`, `kotlin = "2.2.10"`, `ksp = "2.3.6"`

---

## Code Style Rules

- **No comments explaining what code does** — names should be self-explanatory.
- **One comment is allowed per non-obvious invariant** — e.g., `// UI-only sort: sortedBy is stable`.
- **No TODO comments** — either implement it now or track it externally.
- **No unused imports** — fix immediately; they indicate a removed feature wasn't fully cleaned up.
- **No hardcoded strings in UI** — text belongs in the composable as a literal, not in ViewModel or state.
- `internal` visibility for composables that are implementation details of a screen file.
- Prefer `val` over `var` everywhere except `rememberSaveable` local state.

---

## Incremental Implementation Strategy

**Rule: one logical change per session. Explain and verify before the next.**

For any UI change:
1. Identify the composable to change (consult risk map above).
2. Confirm it has no sync-related `LaunchedEffect` or ViewModel calls that would be affected.
3. Make the smallest possible edit — one visual property at a time.
4. After each change: state what changed, why it's safe, confirm no sync logic was touched.
5. Build: `./gradlew clean :app:assembleDebug --no-build-cache`.

For any data/logic change:
1. Read `GroceryRepository.kt` and the relevant ViewModel fully before starting.
2. Identify all places the changed method/field is called.
3. Make the change.
4. Verify `SyncOrigin.Local` is still emitted where required.
5. Build and run unit tests.

---

## UI Modification Workflow

```
1. Identify composable
       ↓
2. Check zone map (SAFE / HIGH RISK / NEVER TOUCH)
       ↓
3. Confirm: no LaunchedEffect in the target block
           no new ViewModel calls being added
           no sync-related state being read/written
       ↓
4. Edit — visual properties only:
   - Modifier (padding, size, alpha, clip)
   - MaterialTheme.colorScheme.* tokens
   - MaterialTheme.typography.* styles
   - elevation, shape, shadow
   ↓
5. Derive display data locally (sort, filter, count)
   — never push display logic to ViewModel
       ↓
6. Build and visually verify
```

**Prefer existing components before creating new ones:**
- `SectionCard` — card with a header label
- `FormTopBar` — top bar for form screens with save action
- `ConfirmationDialog` — yes/no dialog
- `SuggestionAutoCompleteField` — autocomplete text field
- `GroceryForm` — full create/edit form

---

## Forbidden Patterns

```kotlin
// ❌ ViewModel call inside a display branch
if (ui.items.isEmpty()) {
    vm.someTrackingCall()
}

// ❌ Hardcoded color
Text("Hello", color = Color(0xFF4C662B))

// ❌ fillMaxSize() as a sibling in Column with other children
Column {
    Row { ... }
    LazyColumn(Modifier.fillMaxSize()) { ... }  // clips the Row
}

// ❌ Bypassing isInteractionEnabled
IconButton(onClick = onDelete) { ... }  // in a list screen without the guard

// ❌ New ViewModel state field for display logic
data class UiState(val sortedItems: List<Item>)  // sort in the composable instead

// ❌ Touching AppContainer user cache fields
container.cachedUid = null  // this will break per-user DB isolation

// ❌ Hard-deleting a list
dao.deleteList(id)  // use dao.markListDeleted(id, deletedAt, updatedAt) instead

// ❌ Calling repository.insertList() with remote data
repo.insertList(remoteList)  // use repo.applyRemoteList() instead

// ❌ Reordering LaunchedEffects in GroceryListScreen
// The four blocks have a specific execution order that maintains sync state consistency
```

---

## Preferred Patterns

```kotlin
// ✅ Derive display data in the composable
val displayItems = ui.groceryItems.sortedBy { it.isChecked }
val visibleItems = if (hideChecked) displayItems.filter { !it.isChecked } else displayItems

// ✅ Local UI toggle that survives rotation
var hideChecked by rememberSaveable { mutableStateOf(false) }

// ✅ Semantic color tokens
tint = MaterialTheme.colorScheme.error          // delete actions
tint = MaterialTheme.colorScheme.primary        // primary actions
color = MaterialTheme.colorScheme.onSurfaceVariant  // secondary text

// ✅ Card elevation for visual depth
Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) { ... }

// ✅ Lambda progress form avoids unnecessary recomposition
LinearProgressIndicator(progress = { checkedCount.toFloat() / totalCount.toFloat() })

// ✅ weight(1f) for LazyColumn inside Column
Column(Modifier.fillMaxSize()) {
    Row { /* toolbar row */ }
    LazyColumn(Modifier.weight(1f)) { ... }
}

// ✅ New component param with default — zero call site changes
fun SectionCard(
    title: String,
    showBadge: Boolean = false,
    content: @Composable () -> Unit,
)
```

---

## Debugging Workflow

**Build fails — Compose compiler error:**
```bash
./gradlew clean :app:assembleDebug --no-build-cache 2>&1 | grep -A5 "error:"
```
Check for: missing `@OptIn`, wrong lambda signature, incompatible Compose BOM version.

**Runtime crash on the list screen:**
- First suspect: `GroceryListViewModel` — check `syncOrigin` collection and `maybeAutoSync`.
- Second suspect: `AppContainer` — per-user cache may have stale state after sign-out/sign-in.

**Sync not updating UI:**
- Verify `SyncOrigin.Local` is still emitted in `GroceryRepository` after mutations.
- Verify `GroceryListViewModel` still subscribes to `syncOrigin` flow.
- Verify `startRealTime()` is being called in `AppRoot.kt` after login.

**Navigation loops or double-navigation:**
- `isNavigating` flag is not being set via `vm.startNavigation()` before calling the navigation lambda.

**Preview broken in IDE:**
- Check for `@OptIn` missing on the preview composable.
- Check that preview composable is wrapped in `GroceryVaultTheme { }`.

---

## PR / Change Strategy

- **One concern per PR** — UI changes separate from logic changes.
- **Never mix sync logic changes with UI changes** in the same commit.
- **UI-only PRs** need no data-layer review — just visual and build verification.
- **Any change to Repository, ViewModel, or Firebase services** requires explicit explanation of sync impact before merging.
- **Before merging**: build passes, no lint errors, manually verified the changed screen on device/emulator.
