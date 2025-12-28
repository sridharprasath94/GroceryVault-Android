package com.flash.groceryVault.ui.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.flash.groceryVault.data.GroceryListEntity
import com.flash.groceryVault.ui.screens.listGrocery.GroceryListCard
import com.flash.groceryVault.ui.screens.listGrocery.GroceryListRowUi
import com.flash.groceryVault.ui.theme.GroceryVaultTheme

@Preview(showBackground = true)
@Composable
fun GroceryListCardPreview() {
    GroceryVaultTheme {
        GroceryListCard(
            row = GroceryListRowUi(
                list = GroceryListEntity(
                    id = 1,
                    title = "Weekly groceries",
                    description = "For weekend cooking",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                ),
                itemCount = 8,
                checkedCount = 3
            ),
            onOpen = {},
            onEdit = {},
            onDelete = {}
        )
    }
}
