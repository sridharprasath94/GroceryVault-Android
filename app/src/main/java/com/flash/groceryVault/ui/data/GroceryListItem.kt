package com.flash.groceryVault.ui.data

import com.flash.groceryVault.data.GroceryListEntity


data class GroceryListItem(
    val title: String,
    val createdAtText: String,
    val detailText: String,
    val list: GroceryListEntity,
)