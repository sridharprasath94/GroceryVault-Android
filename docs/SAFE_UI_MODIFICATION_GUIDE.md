# Safe UI Modification Guide

## Risk ratings at a glance

| Area | Risk | Reason |
|------|------|--------|
| `ui/theme/` (Color.kt, Theme.kt, Type.kt) | ✅ Safe | Pure design tokens |
| `ui/components/` (all 10 files) | ✅ Safe | Pure presentational composables |
| GroceryDetailScreen.kt | ✅ Safe | Read + toggle only, no sync |
| CreateGroceryScreen.kt | ✅ Safe | Local form, simple DB insert |
| EditGroceryScreen.kt | ✅ Safe | Local form, simple DB update |
| AuthScreen.kt | ⚠️ Caution | GoogleSignIn activity result wiring |
| GroceryListScreen.kt — Card/empty state/FAB | ✅ Safe | Purely presentational sections |
| GroceryListScreen.kt — LaunchedEffects | 🚫 Do not touch | Sync state machine |
| GroceryListViewModel.kt | 🚫 Do not touch | Sync orchestration |
| FirestoreSyncService.kt | 🚫 Do not touch | Two-way sync algorithm |
| GroceryRepository.kt — SyncOrigin | 🚫 Do not touch | Drives sync status UI |
| AppContainer.kt | 🚫 Do not touch | Per-user cache management |

---

## What you can safely change

### Theme level (affects entire app)
- Colors in `Color.kt` — all values are referenced by name in `Theme.kt`, never hardcoded in screens
- Typography in `Type.kt` — swap Inter for another Google Font by changing the `FontFamily`
- Shape tokens — add a `Shapes.kt` and pass it to `MaterialTheme(shapes = ...)` in `Theme.kt`

### Components (`ui/components/`)
- Any visual styling: padding, colors, typography, elevation
- Add new optional parameters with defaults — existing call sites won't break
- Extract sub-composables within a component file

### GroceryListCard
- Padding, typography styles, icon tints
- Card elevation and shape
- Do NOT change `onOpen`, `onEdit`, `onDelete` callback wiring

### GroceryListScreen — empty state
- The entire `if (ui.groceryListItems.isEmpty())` branch is safe to redesign
- Do NOT add ViewModel calls inside this branch

### GroceryDetailScreen
- TopAppBar title, subtitle, icon tints
- Item row layout (padding, typography, alpha, text decoration)
- Loading state layout (currently just `Text("Loading…") + CircularProgressIndicator`)
- Do NOT add new click handlers that call ViewModel functions not already in the screen

### CreateGroceryScreen / EditGroceryScreen
- FormTopBar appearance
- GroceryForm layout and styling
- Saving overlay appearance
- Do NOT touch the `LaunchedEffect` blocks or `repeatOnLifecycle` blocks

---

## Risky state variables — never rename or remove

These are read across the ViewModel↔Screen boundary and are part of the sync state machine:

```kotlin
// GroceryListUiState
isSyncing          // drives PullToRefreshBox.isRefreshing + SyncStatusIcon
isCloudSynced      // drives SyncStatusIcon + prefs write
lastSyncedAt       // used to detect local-newer-than-cloud condition
didAutoSync        // prevents duplicate auto-sync on launch
isNavigating       // gates event emission to prevent races
isLoadingData      // drives interaction blocking overlay
```

### Risky LaunchedEffects in GroceryListScreen (do not touch)

```kotlin
// Line 85: triggers vm.onScreenVisible() on lifecycle STARTED
LaunchedEffect(lifecycleOwner) { ... }

// Line 90: restores cloud sync status from SharedPreferences
LaunchedEffect(Unit) { val synced = prefs.getBoolean... }

// Line 96: detects local-newer-than-cloud and marks unsynced
LaunchedEffect(ui.groceryListItems, ui.lastSyncedAt) { ... }

// Line 105: triggers auto-sync if needed
LaunchedEffect(ui.isCloudSynced, ui.lastSyncedAt, ui.groceryListItems) {
    vm.maybeAutoSync()
}
```

### SharedPreferences keys (do not rename)
```kotlin
private const val cloudSyncedStatusKey = "cloud_synced"
private const val cloudLastSyncedTimeKey = "cloud_last_synced_at"
```

---

## Safe modification patterns

### Adding a visual property to a card
```kotlin
// Before
Card(modifier = Modifier.fillMaxWidth()) { ... }

// After — safe: elevation is purely visual
Card(
    modifier = Modifier.fillMaxWidth(),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
) { ... }
```

### Adding a color to existing text
```kotlin
// Before
Text(item.name, style = MaterialTheme.typography.bodyLarge)

// After — safe: color is read-only, no state impact
Text(
    item.name,
    style = MaterialTheme.typography.bodyLarge,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
)
```

### Adding a new composable parameter with a default
```kotlin
// Before (existing call sites still compile)
fun SectionCard(title: String, content: @Composable () -> Unit)

// After — safe: default value means no call site changes needed
fun SectionCard(
    title: String,
    showDivider: Boolean = false,  // new, has default
    content: @Composable () -> Unit,
)
```

### Deriving display data from existing state
```kotlin
// Safe: reading state that's already in the composable
val checkedCount = ui.groceryItems.count { it.isChecked }
val totalCount = ui.groceryItems.size
// Use these for display only — do not write them back to ViewModel
```

---

## Anti-patterns to avoid

### Don't add new ViewModel calls in display branches
```kotlin
// BAD: unexpected ViewModel side-effect in display code
if (ui.groceryListItems.isEmpty()) {
    vm.trackEmptyState()  // ← don't add this
    ...
}
```

### Don't bypass the isInteractionEnabled guard
```kotlin
// BAD: allows taps during sync/navigation
IconButton(onClick = onEdit) { ... }  // in GroceryListContent without checking isInteractionEnabled
```

### Don't change SharedPreferences read/write in GroceryListScreen
The four `LaunchedEffect` blocks and the `SyncNow` event handler maintain a carefully ordered state machine. Reordering or removing any of them can cause the sync status badge to show stale data or trigger duplicate syncs.

---

## Quick wins still available

| Change | File | Effort |
|--------|------|--------|
| TopAppBar title bold weight | Theme.kt `titleLarge` | 1 line |
| Swap `Toast` for `Snackbar` in list screen | GroceryListScreen.kt | Medium |
| Add `HorizontalDivider` between list items | GroceryListScreen.kt — LazyColumn | 2 lines |
| `LinearProgressIndicator` for checked progress | GroceryDetailScreen.kt | 5 lines |
| Description italic style in detail view | GroceryDetailScreen.kt | 1 line |
