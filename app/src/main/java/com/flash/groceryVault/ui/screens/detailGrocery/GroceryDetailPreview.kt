package com.flash.groceryVault.ui.screens.detailGrocery

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.flash.groceryVault.data.GroceryItemEntity
import com.flash.groceryVault.ui.theme.GroceryVaultTheme

private fun fakeGroceryDetailUiState(
    isLoading: Boolean = false
): GroceryDetailUiState {
    return GroceryDetailUiState(
        title = "Weekly Groceries",
        description = "Items to buy for the coming week",
        createdAt = "12 Mar 2026, 18:45",
        groceryItems = listOf(
            GroceryItemEntity(
                name = "Milk",
                listId = 1,
                isChecked = false
            ),
            GroceryItemEntity(
                name = "Eggs",
                listId = 2,
                isChecked = true
            ),
            GroceryItemEntity(
                name = "Bread",
                listId = 3,
                isChecked = false
            )
        ),
        isLoadingData = isLoading,
        isNavigating = false
    )
}

@Composable
private fun GroceryDetailFormPreviewWrapper(
    ui: GroceryDetailUiState
) {
    GroceryVaultTheme {
        GroceryDetailForm(
            ui = ui,
            onBack = {},
            onEdit = {},
            onToggleItemChecked = { _: Long, _: Boolean -> }
        )
    }
}

@Preview(
    name = "Grocery Detail Loaded",
    showBackground = true
)
@Preview(
    name = "Grocery Detail Loaded – Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun GroceryDetailLoadedFormPreview() {
    GroceryDetailFormPreviewWrapper(
        ui = fakeGroceryDetailUiState(),
    )
}

@Preview(
    name = "Grocery Detail Loading",
    showBackground = true
)
@Preview(
    name = "Grocery Detail Loading – Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun GroceryDetailFormPreview_Loading() {
    GroceryDetailFormPreviewWrapper(
        ui = fakeGroceryDetailUiState(isLoading = true),
    )
}