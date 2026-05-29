package com.flash.groceryVault.ui.screens.detailGrocery

import android.text.format.DateFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.groceryVault.data.GroceryItemEntity
import com.flash.groceryVault.data.GroceryRepository
import com.flash.groceryVault.data.SuggestionType
import com.flash.groceryVault.data.SuggestionsRepository
import com.flash.groceryVault.ui.util.DateFormats
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
    data class ShowUndo(val message: String) : GroceryDetailEvent
    object OnBackClicked : GroceryDetailEvent
    object OnEditClicked : GroceryDetailEvent
}

data class GroceryDetailUiState(
    val title: String = "",
    val description: String? = "",
    val updatedAt: String = "",
    val groceryItems: List<GroceryItemEntity> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val isLoadingData: Boolean = false,
    val isNavigating: Boolean = false,
)

class GroceryDetailViewModel(
    val groceryRepository: GroceryRepository,
    private val suggestionsRepository: SuggestionsRepository,
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

    private var lastRemoved: GroceryItemEntity? = null

    init {
        viewModelScope.launch {
            suggestionsRepository.observeAllMerged(SuggestionType.GROCERY_ITEM).collect { list ->
                _ui.update { it.copy(suggestions = list) }
            }
        }
        // Observe grocery data to populate UI state
        viewModelScope.launch {
            groceryRepository.observeListWithItems(listId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
                .collect { groceryListWithItems ->
                    if (groceryListWithItems != null) {
                        _ui.update {
                            it.copy(
                                title = groceryListWithItems.list.title,
                                description = groceryListWithItems.list.description,
                                updatedAt = DateFormat.format(
                                    DateFormats.LIST_DATE_TIME_WITH_YEAR,
                                    groceryListWithItems.list.updatedAt
                                ).toString(),
                                groceryItems = groceryListWithItems.items,
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

    fun toggleChecked(itemId: Long, newValue: Boolean) {
        // Optimistic UI update
        val previousItems = _ui.value.groceryItems
        _ui.update {
            it.copy(
                groceryItems = it.groceryItems.map { item ->
                    if (item.id == itemId) item.copy(isChecked = newValue) else item
                }
            )
        }

        viewModelScope.launch {
            try {
                groceryRepository.setItemChecked(
                    itemId = itemId,
                    checked = newValue
                )
            } catch (e: Exception) {
                // Rollback UI state on failure
                _ui.update { it.copy(groceryItems = previousItems) }
                toast("Failed to update item: ${e.message}")
            }
        }
    }

    fun quickAddItem(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            try {
                groceryRepository.addItem(listId = listId, name = trimmed)
                suggestionsRepository.add(SuggestionType.GROCERY_ITEM, trimmed)
            } catch (e: Exception) {
                toast("Failed to add item: ${e.message}")
            }
        }
    }

    fun removeItem(item: GroceryItemEntity) {
        lastRemoved = item
        viewModelScope.launch {
            try {
                groceryRepository.deleteItem(item.id)
                emitIfAllowed(GroceryDetailEvent.ShowUndo("Removed ${item.name}"))
            } catch (e: Exception) {
                lastRemoved = null
                toast("Failed to remove item: ${e.message}")
            }
        }
    }

    fun undoRemove() {
        val item = lastRemoved ?: return
        lastRemoved = null
        viewModelScope.launch {
            try {
                groceryRepository.restoreItem(item)
            } catch (e: Exception) {
                toast("Failed to undo: ${e.message}")
            }
        }
    }

    fun uncheckAll() {
        viewModelScope.launch {
            try {
                groceryRepository.uncheckAll(listId)
            } catch (e: Exception) {
                toast("Failed to reset items: ${e.message}")
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
