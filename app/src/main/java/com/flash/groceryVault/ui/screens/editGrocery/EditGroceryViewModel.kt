package com.flash.groceryVault.ui.screens.editGrocery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.groceryVault.data.GroceryListWithItems
import com.flash.groceryVault.data.SuggestionType
import com.flash.groceryVault.di.AppContainer
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class EditGroceryUiState(
    val isSaving: Boolean = false,
)

class EditGroceryViewModel(
    private val container: AppContainer,
    private val listId: Long,
) : ViewModel() {

    private val repo = container.groceryRepository
    private val suggestionsRepo = container.suggestionsRepository

    val data: StateFlow<GroceryListWithItems?> =
        repo.observeListWithItems(listId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _ui = MutableStateFlow(EditGroceryUiState())
    val ui: StateFlow<EditGroceryUiState> = _ui.asStateFlow()

    fun save(
        title: String,
        description: String?,
        items: List<Pair<String, Boolean>>,
        onDone: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) {
            onError("Title is required")
            return
        }

        viewModelScope.launch {
            _ui.value = _ui.value.copy(isSaving = true)
            runCatching {
                repo.updateList(
                    id = listId,
                    title = cleanTitle,
                    description = description,
                    items = items
                )
                suggestionsRepo.addMany(SuggestionType.GROCERY_ITEM, items.map { it.first })
            }.onSuccess {
                _ui.value = _ui.value.copy(isSaving = false)
                onDone()
            }.onFailure {
                _ui.value = _ui.value.copy(isSaving = false)
                onError(it.message ?: "Save failed")
            }
        }
    }
}
