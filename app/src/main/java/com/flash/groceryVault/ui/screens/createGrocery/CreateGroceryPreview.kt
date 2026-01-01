@file:OptIn(ExperimentalMaterial3Api::class)

package com.flash.groceryVault.ui.screens.createGrocery

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.flash.groceryVault.ui.components.GroceryItemFormRow
import com.flash.groceryVault.ui.theme.GroceryVaultTheme

private fun previewCreateGroceryUiState(): CreateGroceryUiState =
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


@Preview(
    name = "Create Grocery – Light",
    showBackground = true
)
@Preview(
    name = "Create Grocery – Dark",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun CreateGroceryFormPreview() {
    GroceryVaultTheme {
        CreateGroceryForm(
            ui = previewCreateGroceryUiState(),
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