package com.flash.groceryVault.ui.screens.detailGrocery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.groceryVault.data.GroceryListWithItems
import com.flash.groceryVault.di.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GroceryDetailViewModel(
    private val container: AppContainer,
    listId: Long,
) : ViewModel() {

    private val repo = container.groceryRepository

    val data: StateFlow<GroceryListWithItems?> =
        repo.observeListWithItems(listId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun toggleChecked(itemId: Long, checked: Boolean) {
        viewModelScope.launch {
            repo.setItemChecked(itemId, checked)
        }
    }
}
