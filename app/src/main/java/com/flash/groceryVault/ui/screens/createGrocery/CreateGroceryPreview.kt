@file:OptIn(ExperimentalMaterial3Api::class)

package com.flash.groceryVault.ui.screens.createGrocery

import android.content.res.Configuration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.flash.groceryVault.ui.components.GroceryItemFormRow
import com.flash.groceryVault.ui.theme.GroceryVaultTheme


private fun previewCreateGroceryUiContentState(
    groceryItems: List<GroceryItemFormRow> = listOf(
        GroceryItemFormRow(name = "Milk"),
        GroceryItemFormRow(name = "Eggs"),
        GroceryItemFormRow(name = "Rice"),
        GroceryItemFormRow(name = "Vegetables")
    ),
    isSaving: Boolean = false,
): CreateGroceryUiState =
    CreateGroceryUiState(
        title = "Weekly Groceries",
        description = "Buy items for the coming week",
        groceryItems = groceryItems,
        suggestions = listOf("Milk", "Eggs", "Rice", "Vegetables", "Bread"),
        isSaving = isSaving,
        isNavigating = false
    )

@Composable
private fun CreateGroceryFormPreviewWrapper(
    ui: CreateGroceryUiState
) {
    GroceryVaultTheme {
        CreateGroceryForm(
            ui = ui,
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
    name = "Create Grocery Content",
    showBackground = true
)
@Preview(
    name = "Create Grocery Content – Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun CreateGroceryFormPreview() {
    GroceryVaultTheme {
        CreateGroceryFormPreviewWrapper(
            ui = previewCreateGroceryUiContentState(),
        )
    }
}

@Preview(
    name = "Create Grocery with No Items",
    showBackground = true
)
@Preview(
    name = "Create Grocery with No Items – Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun CreateGroceryFormNoItemsPreview() {
    GroceryVaultTheme {
        CreateGroceryFormPreviewWrapper(
            ui = previewCreateGroceryUiContentState(groceryItems = emptyList()),
        )
    }
}


@Preview(
    name = "Create Grocery Saving",
    showBackground = true
)
@Preview(
    name = "Create Grocery Saving – Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun CreateGroceryFormSavingPreview() {
    GroceryVaultTheme {
        CreateGroceryFormPreviewWrapper(
            ui = previewCreateGroceryUiContentState(isSaving = true),
        )
    }
}

