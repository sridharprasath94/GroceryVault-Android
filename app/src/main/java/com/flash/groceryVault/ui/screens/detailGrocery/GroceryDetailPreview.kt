package com.flash.groceryVault.ui.screens.detailGrocery

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.flash.groceryVault.ui.components.GroceryItemFormRow
import com.flash.groceryVault.ui.theme.GroceryVaultTheme

private fun fakeGroceryDetailUiState(
    isLoading: Boolean = false
): GroceryDetailUiState {
    return GroceryDetailUiState(
        title = "Weekly Groceries",
        description = "Items to buy for the coming week",
        createdAt = "12 Mar 2026, 18:45",
        groceryItems = listOf(
            GroceryItemFormRow(
                name = "Milk",
                isChecked = false
            ),
            GroceryItemFormRow(
                name = "Eggs",
                isChecked = true
            ),
            GroceryItemFormRow(
                name = "Bread",
                isChecked = false
            )
        ),
        isLoadingData = isLoading,
        isNavigating = false
    )
}

@Preview(
    name = "Grocery Detail Loaded",
    showBackground = true
)
@Preview(
    name = "Grocery Detail Loaded – Dark",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun GroceryDetailPreview_Loaded() {
    GroceryVaultTheme {
        GroceryDetailForm(
            ui = fakeGroceryDetailUiState(),
            onBack = {},
            onEdit = {},
            onToggleItemChecked = {}
        )
    }
}

@Preview(
    name = "Grocery Detail Loading",
    showBackground = true
)
@Preview(
    name = "Grocery Detail Loading – Dark",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun GroceryDetailPreview_Loading() {
    GroceryVaultTheme {
        GroceryDetailForm(
            ui = fakeGroceryDetailUiState(isLoading = true),
            onBack = {},
            onEdit = {},
            onToggleItemChecked = {}
        )
    }
}