package com.flash.groceryVault.ui.screens.editGrocery

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.flash.groceryVault.ui.components.GroceryItemFormRow
import com.flash.groceryVault.ui.theme.GroceryVaultTheme

private fun previewEditGroceryUiContentState(): EditGroceryUiState =
    EditGroceryUiState(
        title = "Weekly Groceries",
        description = "Buy items for the coming week",
        groceryItems = listOf(
            GroceryItemFormRow(name = "Milk"),
            GroceryItemFormRow(name = "Eggs"),
            GroceryItemFormRow(name = "Rice"),
            GroceryItemFormRow(name = "Vegetables")
        ),
        suggestions = listOf("Milk", "Eggs", "Rice", "Vegetables", "Bread"),
        isSaving = false,
        isLoadingData = false,
        isNavigating = false
    )


private fun previewEditGroceryUiLoadingState(): EditGroceryUiState =
    EditGroceryUiState(
        title = "Weekly Groceries",
        description = "Buy items for the coming week",
        groceryItems = listOf(
            GroceryItemFormRow(name = "Milk"),
            GroceryItemFormRow(name = "Eggs"),
            GroceryItemFormRow(name = "Rice"),
            GroceryItemFormRow(name = "Vegetables")
        ),
        suggestions = listOf("Milk", "Eggs", "Rice", "Vegetables", "Bread"),
        isSaving = false,
        isLoadingData = true,
        isNavigating = false
    )

private fun previewEditGroceryUiSavingState(): EditGroceryUiState =
    EditGroceryUiState(
        title = "Weekly Groceries",
        description = "Buy items for the coming week",
        groceryItems = listOf(
            GroceryItemFormRow(name = "Milk"),
            GroceryItemFormRow(name = "Eggs"),
            GroceryItemFormRow(name = "Rice"),
            GroceryItemFormRow(name = "Vegetables")
        ),
        suggestions = listOf("Milk", "Eggs", "Rice", "Vegetables", "Bread"),
        isSaving = true,
        isLoadingData = false,
        isNavigating = false
    )

@Preview(
    name = "Create Grocery Content – Light",
    showBackground = true
)
@Preview(
    name = "Create Grocery Content – Dark",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun CreateGroceryFormContentPreview() {
    GroceryVaultTheme {
        EditGroceryForm(
            ui = previewEditGroceryUiContentState(),
            onBack = {},
            onSave = {},
            onTitleChange = {},
            onDescriptionChange = {},
            onItemChange = { _, _ -> },
            onItemRemove = {},
            onAddItem = {}
        )
    }
}


@Preview(
    name = "Create Grocery Saving – Light",
    showBackground = true
)
@Preview(
    name = "Create Grocery Saving – Dark",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun CreateGroceryFormSavingPreview() {
    GroceryVaultTheme {
        EditGroceryForm(
            ui = previewEditGroceryUiSavingState(),
            onBack = {},
            onSave = {},
            onTitleChange = {},
            onDescriptionChange = {},
            onItemChange = { _, _ -> },
            onItemRemove = {},
            onAddItem = {}
        )
    }
}

@Preview(
    name = "Create Grocery Loading – Light",
    showBackground = true
)
@Preview(
    name = "Create Grocery Loading – Dark",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun CreateGroceryFormLoadingPreview() {
    GroceryVaultTheme {
        EditGroceryForm(
            ui = previewEditGroceryUiLoadingState(),
            onBack = {},
            onSave = {},
            onTitleChange = {},
            onDescriptionChange = {},
            onItemChange = { _, _ -> },
            onItemRemove = {},
            onAddItem = {}
        )
    }
}