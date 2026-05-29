package com.flash.groceryVault.ui.screens.editGrocery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.groceryVault.data.GroceryListWithItems
import com.flash.groceryVault.data.GroceryRepository
import com.flash.groceryVault.data.SuggestionType
import com.flash.groceryVault.data.SuggestionsRepository
import com.flash.groceryVault.ui.components.GroceryItemFormRow
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface EditGroceryEvent {
    data class Toast(val message: String) : EditGroceryEvent
    object OnBackClicked : EditGroceryEvent
    data class OnFinishedSaving(val id: Long) : EditGroceryEvent
}

data class EditGroceryUiState(
    val title: String = "",
    val description: String = "",
    val groceryItems: List<GroceryItemFormRow> = listOf(GroceryItemFormRow()),
    val suggestions: List<String> = emptyList(),
    val isLoadingData: Boolean = false,
    val isSaving: Boolean = false,
    val isNavigating: Boolean = false,
)

class EditGroceryViewModel(
    val groceryRepository: GroceryRepository,
    val suggestionsRepository: SuggestionsRepository,
    private val listId: Long,
) : ViewModel() {
    private val _ui = MutableStateFlow(EditGroceryUiState())
    val ui: StateFlow<EditGroceryUiState> = _ui.asStateFlow()

    private val _events = MutableSharedFlow<EditGroceryEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<EditGroceryEvent> = _events.asSharedFlow()

    val data: StateFlow<GroceryListWithItems?> =
        groceryRepository.observeListWithItems(listId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        // Observe grocery data to populate UI state
        viewModelScope.launch {
            groceryRepository.observeListWithItems(listId)
                .onStart { _ui.update { it.copy(isLoadingData = true) } }
                .filterNotNull()
                .collect { groceryListWithItems ->
                    _ui.update {
                        it.copy(
                            title = groceryListWithItems.list.title,
                            description = groceryListWithItems.list.description ?: "",
                            groceryItems = groceryListWithItems.items.map { item ->
                                GroceryItemFormRow(
                                    name = item.name,
                                    isChecked = item.isChecked
                                )
                            }.ifEmpty { listOf(GroceryItemFormRow()) },
                            isLoadingData = false
                        )
                    }
                }
        }

        // Observe suggestions
        viewModelScope.launch {
            suggestionsRepository.observeAllMerged(SuggestionType.GROCERY_ITEM)
                .collect { suggestions ->
                    _ui.update { it.copy(suggestions = suggestions) }
                }
        }
    }

    fun updateTitle(value: String) {
        _ui.update { it.copy(title = value) }
    }

    fun updateDescription(value: String) {
        _ui.update { it.copy(description = value) }
    }

    fun onAddGroceryItem() {
        _ui.update { it.copy(groceryItems = it.groceryItems + GroceryItemFormRow()) }
    }

    fun quickAddRow(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        _ui.update { it.copy(groceryItems = it.groceryItems + GroceryItemFormRow(name = trimmed)) }
    }

    fun onGroceryItemChanged(index: Int, row: GroceryItemFormRow) {
        _ui.update { state ->
            val list = state.groceryItems.toMutableList()
            if (index in list.indices) list[index] = row
            state.copy(groceryItems = list)
        }
    }

    fun onGroceryItemRemoved(index: Int) {
        _ui.update { state ->
            val list = state.groceryItems.toMutableList()
            if (list.size <= 1) {
                return@update state.copy(groceryItems = listOf(GroceryItemFormRow()))
            }
            if (index in list.indices) list.removeAt(index)
            state.copy(groceryItems = list)
        }
    }

    fun save() {
        val state = _ui.value
        val cleanTitle = state.title.trim()
        val cleanDesc = state.description.trim().ifEmpty { null }
        val items = state.groceryItems.map {
            it.name.trim() to it.isChecked
        }

        viewModelScope.launch {
            try {
                _ui.value = _ui.value.copy(isSaving = true)
                groceryRepository.updateList(
                    id = listId,
                    title = cleanTitle,
                    description = cleanDesc,
                    items = items
                )
                suggestionsRepository.addMany(SuggestionType.GROCERY_ITEM, items.map { it.first })
                onFinishedSaving(listId)
            } catch (e: Exception) {
                toast(e.message ?: "Failed to save")
            } finally {
                _ui.update { it.copy(isSaving = false) }
            }

        }
    }

    private fun onFinishedSaving(id: Long) {
        emitIfAllowed(EditGroceryEvent.OnFinishedSaving(id))
    }

    fun requestBack() = emitIfAllowed(EditGroceryEvent.OnBackClicked)

    private fun toast(message: String) {
        emitIfAllowed(EditGroceryEvent.Toast(message))
    }

    fun startNavigation() {
        _ui.update { it.copy(isNavigating = true) }
    }

    fun onScreenVisible() {
        _ui.update { it.copy(isNavigating = false) }
    }

    private fun emitIfAllowed(event: EditGroceryEvent) {
        if (!_ui.value.isNavigating) {
            _events.tryEmit(event)
        }
    }
}
