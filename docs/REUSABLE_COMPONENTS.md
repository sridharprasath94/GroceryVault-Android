# Reusable Components

All files live in `ui/components/`. All are purely presentational — no ViewModel dependencies, no Firebase calls.

---

## SectionCard
**File**: `SectionCard.kt`

Card wrapper with a header label and slotted content.

```kotlin
SectionCard(title = "Groceries") {
    // any composable content
}
```

**Parameters**:
- `title: String` — header text (currently styled `titleSmall` in `primary` color)
- `modifier: Modifier = Modifier`
- `contentPadding: PaddingValues = PaddingValues(12.dp)`
- `content: @Composable () -> Unit`

**Used in**: GroceryDetailScreen (wraps the items list), CreateGroceryScreen (via GroceryForm)

**Safe to extend**: Add an optional `trailingIcon` or `subtitle` parameter with a default of `null`.

---

## FormTopBar
**File**: `FormTopBar.kt`

TopAppBar for Create/Edit screens with back navigation and a primary action button.

```kotlin
FormTopBar(
    title = "New Grocery List",
    actionLabel = "Save",
    isInteractionEnabled = !ui.isSaving,
    isActionInProgress = ui.isSaving,
    onBack = onBack,
    onPrimaryAction = onSave,
)
```

**Used in**: CreateGroceryScreen, EditGroceryScreen

**Loading state**: When `isActionInProgress = true`, the action button shows a `CircularProgressIndicator` instead of the label text.

---

## GroceryForm
**File**: `GroceryFormComponents.kt`

The main form layout shared between Create and Edit. Renders title field, optional description field, and the draggable grocery items list.

```kotlin
GroceryForm(
    padding = scaffoldPaddingValues,
    title = ui.title,
    onTitleChange = vm::updateTitle,
    description = ui.description,
    onDescriptionChange = vm::updateDescription,
    groceryItems = ui.groceryItems,           // List<GroceryItemFormRow>
    suggestions = ui.suggestions,             // List<String>
    onItemChange = vm::onGroceryItemChanged,  // (index, GroceryItemFormRow) -> Unit
    onItemRemove = vm::onGroceryItemRemoved,  // (index) -> Unit
    onAddItem = { vm.onAddGroceryItem() },
)
```

**Used in**: CreateGroceryScreen (`CreateGroceryForm`), EditGroceryScreen (`EditGroceryForm`)

---

## GroceryFormField
**File**: `GroceryFormField.kt`

A single row in the items list: checkbox + autocomplete text field + delete button.

```kotlin
GroceryFormField(
    item = groceryItemFormRow,
    suggestions = listOf("Milk", "Eggs"),
    onItemChange = { updated -> ... },
    onRemove = { ... },
)
```

**Used in**: `GroceryForm` (rendered for each item in the list)

---

## GroceryItemFormRow (data model)
**File**: `GroceryFormModels.kt`

```kotlin
data class GroceryItemFormRow(
    val name: String,
    val isChecked: Boolean,
)
```

Used to represent each item in the Create/Edit forms. Not the same as `GroceryItemEntity` (which is the Room entity).

---

## GroceryEditFields
**File**: `GroceryEditFields.kt`

Title + description `OutlinedTextField` pair used at the top of the form.

```kotlin
GroceryEditFields(
    title = ui.title,
    onTitleChange = onTitleChange,
    description = ui.description,
    onDescriptionChange = onDescriptionChange,
    isEnabled = isInteractionEnabled,
)
```

**Used in**: `GroceryForm`

---

## StandardTextField
**File**: `StandardTextField.kt`

Thin wrapper around Material3 `OutlinedTextField` with standardized keyboard options.

```kotlin
StandardTextField(
    value = text,
    onValueChange = { text = it },
    label = "Title",
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    keyboardActions = KeyboardActions(onNext = { ... }),
)
```

**Used in**: `GroceryEditFields`, `SuggestionAutoCompleteField`

---

## SuggestionAutoCompleteField
**File**: `SuggestionAutoCompleteField.kt`

Text field with prefix-match dropdown suggestions. Has two overloads:
- `String` value variant — simpler, for most cases
- `TextFieldValue` variant — when cursor position matters

```kotlin
SuggestionAutoCompleteField(
    value = itemName,
    onValueChange = { itemName = it },
    suggestions = listOf("Milk", "Eggs", "Bread"),
    label = "Item name",
)
```

**Dropdown behavior**: Shows up to 5 suggestions matching the current prefix. Selecting a suggestion fills the field and collapses the dropdown.

**IME padding**: Animates bottom padding with a 220ms tween when the keyboard appears.

**Used in**: `GroceryFormField`

---

## GroceryImagePicker
**File**: `GroceryImagePicker.kt`

Image selection UI that handles both local URIs (from gallery) and remote URLs (from Firebase Storage).

```kotlin
GroceryImagePicker(
    imageUri = ui.imageUri,         // local Uri?
    imageUrl = ui.imageUrl,         // remote URL String?
    onPickImage = vm::onPickImage,
    onRemoveImage = vm::onRemoveImage,
)
```

**States**:
- No image → shows "Add Photo" button
- Local URI or remote URL → shows image with "Change" / "Remove" options

**Used in**: CreateGroceryScreen, EditGroceryScreen (if image feature is enabled)

---

## ConfirmationDialog
**File**: `ConfirmationDialog.kt`

Material3 `AlertDialog` wrapper for yes/no confirmations.

```kotlin
ConfirmationDialog(
    show = ui.showDeleteDialog,
    title = "Delete list?",
    message = "This cannot be undone.",
    confirmButtonText = "Delete",
    onConfirm = vm::confirmDelete,
    onDismiss = vm::dismissDelete,
)
```

**Used in**: GroceryListScreen (logout + delete dialogs), EditGroceryScreen (delete dialog)

---

## Extension tips

**Adding a new optional param** — existing call sites are unaffected:
```kotlin
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    showBadge: Boolean = false,   // new param, default = false
    content: @Composable () -> Unit,
)
```

**Theming components** — always prefer `MaterialTheme.colorScheme.*` and `MaterialTheme.typography.*` over hardcoded values so dark mode works for free.
