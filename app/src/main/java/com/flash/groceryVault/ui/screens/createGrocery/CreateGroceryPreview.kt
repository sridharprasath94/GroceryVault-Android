@file:OptIn(ExperimentalMaterial3Api::class)

package com.flash.groceryVault.ui.screens.createGrocery

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.flash.groceryVault.ui.components.GroceryItemFormRow
import com.flash.groceryVault.ui.theme.GroceryVaultTheme

private fun previewCreateGroceryUiContentState(): CreateGroceryUiState =
    CreateGroceryUiState(
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
        isNavigating = false
    )

private fun previewCreateGroceryUiSavingState(): CreateGroceryUiState =
    CreateGroceryUiState(
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
fun CreateGroceryFormPreview() {
    GroceryVaultTheme {
        CreateGroceryForm(
            ui = previewCreateGroceryUiContentState(),
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
        CreateGroceryForm(
            ui = previewCreateGroceryUiSavingState(),
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
