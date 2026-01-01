package com.flash.groceryVault.ui.screens.listGrocery

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.flash.groceryVault.data.GroceryListEntity
import com.flash.groceryVault.ui.theme.GroceryVaultTheme

fun fakeGroceryListItems(): List<GroceryListItem> = listOf(
    GroceryListItem(
        list = GroceryListEntity(
            id = 1L,
            title = "Weekly Groceries",
            description = "Vegetables and fruits",
            createdAt = System.currentTimeMillis() - 86_400_000
        ),
        itemCount = 12,
        checkedCount = 4
    ),
    GroceryListItem(
        list = GroceryListEntity(
            id = 2L,
            title = "Party Shopping",
            description = "Snacks and drinks",
            createdAt = System.currentTimeMillis() - 2 * 86_400_000
        ),
        itemCount = 8,
        checkedCount = 0
    )
)

fun fakeGroceryListUiState(): GroceryListUiState =
    GroceryListUiState(
        currentUserUid = "preview-user",
        groceryListItems = fakeGroceryListItems(),
        showMenu = false,
        showLogoutDialog = false,
        deleteListId = null,
        isSyncing = false,
        isCloudSynced = true,
        lastSyncedAt = System.currentTimeMillis() - 60_000,
        didAutoSync = true,
        isLoadingData = false,
        isNavigating = false
    )

fun fakeLoadedState() =
    fakeGroceryListUiState()

fun fakeLoadingState() =
    fakeGroceryListUiState().copy(
        isLoadingData = true,
        groceryListItems = emptyList()
    )

fun fakeEmptyState() =
    fakeGroceryListUiState().copy(
        groceryListItems = emptyList(),
        isLoadingData = false
    )

fun fakeMenuOpenState() =
    fakeGroceryListUiState().copy(
        showMenu = true,
        isNavigating = false
    )

@Composable
private fun GroceryListPreviewWrapper(
    ui: GroceryListUiState
) {
    GroceryVaultTheme {
        GroceryListContent(
            ui = ui,
            onCreate = {},
            onOpenGrocery = {},
            onEditGrocery = {},
            onDeleteGrocery = {},
            onMenuToggle = {},
            onMenuDismiss = {},
            onSyncNow = {},
            onRequestLogout = {},
        )
    }
}

@Preview(
    name = "Loaded",
    showBackground = true
)
@Preview(
    name = "Loaded – Dark",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun GroceryListPreview_Loaded() {
    GroceryListPreviewWrapper(fakeLoadedState())
}

@Preview(
    name = "Loading",
    showBackground = true
)
@Preview(
    name = "Loading – Dark",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun GroceryListPreview_Loading() {
    GroceryListPreviewWrapper(fakeLoadingState())
}

@Preview(
    name = "Empty",
    showBackground = true
)
@Preview(
    name = "Empty – Dark",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun GroceryListPreview_Empty() {
    GroceryListPreviewWrapper(fakeEmptyState())
}

@Preview(
    name = "Menu Open",
    showBackground = true
)

@Preview(
    name = "Menu Open – Dark",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun GroceryListPreview_MenuOpen() {
    GroceryListPreviewWrapper(fakeMenuOpenState())
}