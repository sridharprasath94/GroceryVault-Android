package com.flash.groceryVault.ui.screens.createGrocery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.groceryVault.data.SuggestionType
import com.flash.groceryVault.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateGroceryUiState(
    val isSaving: Boolean = false,
)

class CreateGroceryViewModel(
    private val container: AppContainer
) : ViewModel() {

    private val repo = container.groceryRepository
    private val suggestionsRepo = container.suggestionsRepository

    private val _ui = MutableStateFlow(CreateGroceryUiState())
    val ui: StateFlow<CreateGroceryUiState> = _ui.asStateFlow()

    fun save(
        title: String,
        description: String?,
        items: List<String>,
        onDone: (Long) -> Unit,
        onError: (String) -> Unit,
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) {
            onError("Title is required")
            return
        }

        viewModelScope.launch {
            _ui.value = _ui.value.copy(isSaving = true)
            runCatching {
                val id = repo.createList(cleanTitle, description, items)
                suggestionsRepo.addMany(SuggestionType.GROCERY_ITEM, items)
                id
            }.onSuccess { id ->
                _ui.value = _ui.value.copy(isSaving = false)
                onDone(id)
            }.onFailure {
                _ui.value = _ui.value.copy(isSaving = false)
                onError(it.message ?: "Save failed")
            }
        }
    }
}
