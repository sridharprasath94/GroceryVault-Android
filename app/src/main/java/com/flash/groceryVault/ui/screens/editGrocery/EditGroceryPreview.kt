package com.flash.groceryVault.ui.screens.editGrocery

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.flash.groceryVault.ui.components.GroceryItemFormRow
import com.flash.groceryVault.ui.theme.GroceryVaultTheme

private fun previewEditGroceryUiContentState(
    groceryItems: List<GroceryItemFormRow> = listOf(
        GroceryItemFormRow(name = "Milk"),
        GroceryItemFormRow(name = "Eggs"),
        GroceryItemFormRow(name = "Rice"),
        GroceryItemFormRow(name = "Vegetables")
    ),
    isSaving: Boolean = false,
    isLoadingData: Boolean = false
): EditGroceryUiState =
    EditGroceryUiState(
        title = "Weekly Groceries",
        description = "Buy items for the coming week",
        groceryItems = groceryItems,
        suggestions = listOf("Milk", "Eggs", "Rice", "Vegetables", "Bread"),
        isSaving = isSaving,
        isLoadingData = isLoadingData,
        isNavigating = false
    )

@Composable
private fun EditGroceryFormPreviewWrapper(
    ui: EditGroceryUiState
) {
    GroceryVaultTheme {
        EditGroceryForm(
            ui = ui,
            onBack = {},
            onSave = {},
            onTitleChange = {},
            onDescriptionChange = {},
            onItemChange = { _, _ -> },
            onItemRemove = {},
            onQuickAdd = {}
        )
    }
}

@Preview(
    name = "Edit Grocery Content",
    showBackground = true
)
@Preview(
    name = "Edit Grocery Content – Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun EditGroceryFormContentFormPreview() {
    EditGroceryFormPreviewWrapper(
        ui = previewEditGroceryUiContentState(),
    )
}


@Preview(
    name = "Edit Grocery Saving",
    showBackground = true
)
@Preview(
    name = "Edit Grocery Saving – Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun EditGroceryFormSavingFormPreview() {
    EditGroceryFormPreviewWrapper(
        ui = previewEditGroceryUiContentState(isSaving = true),
    )
}

@Preview(
    name = "Edit Grocery Loading",
    showBackground = true
)
@Preview(
    name = "Edit Grocery Loading – Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun EditGroceryFormLoadingFormPreview() {
    EditGroceryFormPreviewWrapper(
        ui = previewEditGroceryUiContentState(isLoadingData = true),
    )
}

@Preview(
    name = "Edit Grocery with No Items",
    showBackground = true
)
@Preview(
    name = "Edit Grocery with No Items – Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun EditGroceryFormNoItemsFormPreview() {
    EditGroceryFormPreviewWrapper(
        ui = previewEditGroceryUiContentState(groceryItems = emptyList()),
    )
}