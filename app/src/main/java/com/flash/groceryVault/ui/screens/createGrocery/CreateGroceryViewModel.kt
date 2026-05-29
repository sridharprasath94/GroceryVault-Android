package com.flash.groceryVault.ui.screens.createGrocery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.groceryVault.data.GroceryRepository
import com.flash.groceryVault.data.SuggestionType
import com.flash.groceryVault.data.SuggestionsRepository
import com.flash.groceryVault.di.AppContainer
import com.flash.groceryVault.ui.components.GroceryItemFormRow
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.plus
import kotlin.collections.toMutableList

sealed interface CreateGroceryEvent {
    data class Toast(val message: String) : CreateGroceryEvent
    object OnBackClicked : CreateGroceryEvent
    data class OnFinishedSaving(val id: Long) : CreateGroceryEvent
}


data class CreateGroceryUiState(
    val title: String = "",
    val description: String = "",
    val groceryItems: List<GroceryItemFormRow> = listOf(GroceryItemFormRow()),
    val suggestions: List<String> = emptyList(),
    val isSaving: Boolean = false,
    val isNavigating: Boolean = false,
)

class CreateGroceryViewModel(
    val groceryRepository: GroceryRepository,
    val suggestionsRepository: SuggestionsRepository,
) : ViewModel() {
    private val _ui = MutableStateFlow(CreateGroceryUiState())
    val ui: StateFlow<CreateGroceryUiState> = _ui.asStateFlow()

    private val _events = MutableSharedFlow<CreateGroceryEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<CreateGroceryEvent> = _events.asSharedFlow()

    init {
        // Observe suggestions
        viewModelScope.launch {
            suggestionsRepository.observeAllMerged(SuggestionType.GROCERY_ITEM).collect { suggestions ->
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
        val items = state.groceryItems.map { it.name to it.isChecked }

        viewModelScope.launch {
            try {
                _ui.value = _ui.value.copy(isSaving = true)
                val id = groceryRepository.createList(cleanTitle, cleanDesc, items)
                suggestionsRepository.addMany(SuggestionType.GROCERY_ITEM, items .map { it.first })
                onFinishedSaving(id)
            } catch (e: Exception) {
                toast(e.message ?: "Failed to save")
            } finally {
                _ui.update { it.copy(isSaving = false) }
            }

        }
    }


    private fun onFinishedSaving(id: Long) {
        emitIfAllowed(CreateGroceryEvent.OnFinishedSaving(id))
    }

    fun requestBack() = emitIfAllowed(CreateGroceryEvent.OnBackClicked)

    private fun toast(message: String) {
        emitIfAllowed(CreateGroceryEvent.Toast(message))
    }

    fun startNavigation() {
        _ui.update { it.copy(isNavigating = true) }
    }

    fun onScreenVisible() {
        _ui.update { it.copy(isNavigating = false) }
    }

    private fun emitIfAllowed(event: CreateGroceryEvent) {
        if (!_ui.value.isNavigating) {
            _events.tryEmit(event)
        }
    }
}
