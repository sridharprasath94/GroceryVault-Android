package com.flash.groceryVault.ui.screens.detailGrocery

import android.text.format.DateFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.groceryVault.data.GroceryListWithItems
import com.flash.groceryVault.data.GroceryRepository
import com.flash.groceryVault.ui.components.GroceryItemFormRow
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface GroceryDetailEvent {
    data class Toast(val message: String) : GroceryDetailEvent
    object OnBackClicked : GroceryDetailEvent
    object OnEditClicked : GroceryDetailEvent
}

data class GroceryDetailUiState(
    val title: String = "",
    val description: String? = "",
    val createdAt: String = "",
    val groceryItems: List<GroceryItemFormRow> = listOf(GroceryItemFormRow()),
    val isLoadingData: Boolean = false,
    val isNavigating: Boolean = false,
)

class GroceryDetailViewModel(
    val groceryRepository: GroceryRepository,
    private val listId: Long,
) : ViewModel() {
    private val _ui = MutableStateFlow(GroceryDetailUiState())
    val ui: StateFlow<GroceryDetailUiState> = _ui.asStateFlow()

    private val _events = MutableSharedFlow<GroceryDetailEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<GroceryDetailEvent> = _events.asSharedFlow()
    val data: StateFlow<GroceryListWithItems?> =
        groceryRepository.observeListWithItems(listId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        // Observe grocery data to populate UI state
        viewModelScope.launch {
            data.collect { groceryListWithItems ->
                if (groceryListWithItems != null) {
                    _ui.update {
                        it.copy(
                            title = groceryListWithItems.list.title,
                            description = groceryListWithItems.list.description,
                            createdAt = DateFormat.format(
                                "dd MMM yyyy, HH:mm",
                                groceryListWithItems.list.createdAt
                            ).toString(),
                            groceryItems = groceryListWithItems.items.map { item ->
                                GroceryItemFormRow(
                                    name = item.name,
                                    isChecked = item.isChecked
                                )
                            },
                            isLoadingData = false
                        )
                    }
                } else {
                    _ui.update {
                        it.copy(isLoadingData = true)
                    }
                }
            }
        }
    }

    fun toggleChecked(checked: Boolean) {
        viewModelScope.launch {
            try {
                groceryRepository.setItemChecked(listId, checked)
            } catch (e: Exception) {
                toast("Failed to update item: ${e.message}")
            }
        }
    }

    fun requestBack() = emitIfAllowed(GroceryDetailEvent.OnBackClicked)

    fun requestEdit() = emitIfAllowed(GroceryDetailEvent.OnEditClicked)

    private fun toast(message: String) {
        emitIfAllowed(GroceryDetailEvent.Toast(message))
    }

    fun startNavigation() {
        _ui.update { it.copy(isNavigating = true) }
    }

    fun onScreenVisible() {
        _ui.update { it.copy(isNavigating = false) }
    }

    private fun emitIfAllowed(event: GroceryDetailEvent) {
        if (!_ui.value.isNavigating) {
            _events.tryEmit(event)

        }
    }
}
