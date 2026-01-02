package com.flash.groceryVault.ui.screens.listGrocery

import android.content.res.Configuration
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.flash.groceryVault.data.GroceryListEntity
import com.flash.groceryVault.ui.theme.GroceryVaultTheme
import com.flash.groceryVault.ui.util.DateFormats

private fun fakeGroceryListItems(): List<GroceryListItem> = listOf(
    GroceryListItem(
        title = "Weekly Groceries",
        createdAtText = DateFormat.format(
            DateFormats.LIST_DATE_TIME_WITH_YEAR,
            System.currentTimeMillis() - 86_400_000
        ).toString(),
        detailText = "12 items, 4 checked",
        list = GroceryListEntity(
            id = 1L,
            title = "Weekly Groceries",
            description = "Vegetables and fruits",
            createdAt = System.currentTimeMillis() - 86_400_000
        ),
    ),
    GroceryListItem(
        title = "Party Shopping",
        createdAtText = DateFormat.format(
            DateFormats.LIST_DATE_TIME_WITH_YEAR,
            System.currentTimeMillis() - 2 * 86_400_000
        ).toString(),
        detailText = "8 items, 0 checked",
        list = GroceryListEntity(
            id = 2L,
            title = "Party Shopping",
            description = "Snacks and drinks",
            createdAt = System.currentTimeMillis() - 2 * 86_400_000
        ),
    )
)

fun fakeGroceryListUiState(
    isLoadingData: Boolean = false,
    groceryListWithItems: List<GroceryListItem> = fakeGroceryListItems(),
    showMenu: Boolean = false,
    isNavigating: Boolean = false
): GroceryListUiState =
    GroceryListUiState(
        currentUserUid = "preview-user",
        groceryListItems = groceryListWithItems,
        showMenu = showMenu,
        showLogoutDialog = false,
        deleteListId = null,
        isSyncing = false,
        isCloudSynced = true,
        lastSyncedAt = System.currentTimeMillis() - 60_000,
        didAutoSync = true,
        isLoadingData = isLoadingData,
        isNavigating = isNavigating
    )


@Composable
private fun GroceryListContentPreviewWrapper(
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
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun GroceryListContentPreview_Loaded() {
    GroceryListContentPreviewWrapper(
        fakeGroceryListUiState()
    )
}

@Preview(
    name = "Loading",
    showBackground = true
)
@Preview(
    name = "Loading – Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun GroceryListContentPreview_Loading() {
    GroceryListContentPreviewWrapper(
        fakeGroceryListUiState(
            isLoadingData = true,
        )
    )
}

@Preview(
    name = "Empty",
    showBackground = true
)
@Preview(
    name = "Empty – Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun GroceryListContentPreview_Empty() {
    GroceryListContentPreviewWrapper(
        fakeGroceryListUiState(
            groceryListWithItems = emptyList()
        )
    )
}

@Preview(
    name = "Menu Open",
    showBackground = true
)
@Preview(
    name = "Menu Open – Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun GroceryListContentPreview_MenuOpen() {
    GroceryListContentPreviewWrapper(
        fakeGroceryListUiState(
            showMenu = true,
            isNavigating = false
        )
    )
}