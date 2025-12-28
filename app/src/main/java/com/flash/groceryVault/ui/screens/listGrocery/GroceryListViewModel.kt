package com.flash.groceryVault.ui.screens.listGrocery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.groceryVault.data.GroceryListEntity
import com.flash.groceryVault.di.AppContainer
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class GroceryListRowUi(
    val list: GroceryListEntity,
    val itemCount: Int = 0,
    val checkedCount: Int = 0,
)

data class GroceryListUiState(
    val rows: List<GroceryListRowUi> = emptyList(),
    val isSyncing: Boolean = false,
    val isCloudSynced: Boolean = false,
    val lastSyncedAt: Long = 0L,
)

sealed class GroceryListEvent {
    data class Toast(val message: String) : GroceryListEvent()
    data object LoggedOut : GroceryListEvent()
}

class GroceryListViewModel(
    private val container: AppContainer,
) : ViewModel() {

    private val repo = container.groceryRepository

    private val _ui = MutableStateFlow(GroceryListUiState())
    val ui: StateFlow<GroceryListUiState> = _ui.asStateFlow()

    private val _events = MutableSharedFlow<GroceryListEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<GroceryListEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repo.observeLists().collect { lists ->
                // Build rows with counts (small lists: compute on the fly)
                val rows = lists.map { list ->
                    val details = repo.getListWithItemsOnce(list.id)
                    val items = details?.items.orEmpty()
                    GroceryListRowUi(
                        list = list,
                        itemCount = items.size,
                        checkedCount = items.count { it.isChecked }
                    )
                }
                _ui.update { it.copy(rows = rows) }
            }
        }
    }

    fun signOut() {
        container.signOut()
        _events.tryEmit(GroceryListEvent.LoggedOut)
    }

    fun onLocalMutation() {
        // call after create/edit/delete so UI shows "needs sync"
        _ui.update { it.copy(isCloudSynced = false) }
    }

    fun restoreCloudStatus(isCloudSynced: Boolean, lastSyncedAt: Long) {
        _ui.update { it.copy(isCloudSynced = isCloudSynced, lastSyncedAt = lastSyncedAt) }
    }

    fun syncNow(onSuccess: (Long) -> Unit, onFailure: (String) -> Unit) {
        if (_ui.value.isSyncing) return
        _ui.update { it.copy(isSyncing = true) }

        viewModelScope.launch {
            runCatching {
                container.firestoreSyncServiceForCurrentUser().syncNow()
            }.onSuccess {
                val now = System.currentTimeMillis()
                _ui.update { it.copy(isSyncing = false, isCloudSynced = true, lastSyncedAt = now) }
                onSuccess(now)
            }.onFailure {
                _ui.update { it.copy(isSyncing = false, isCloudSynced = false) }
                val msg = it.message ?: "Sync failed"
                _events.tryEmit(GroceryListEvent.Toast(msg))
                onFailure(msg)
            }
        }
    }

    fun deleteList(listId: Long, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            runCatching {
                repo.deleteList(listId)
            }.onSuccess {
                _ui.update { it.copy(isCloudSynced = false) }
                onSuccess()
            }.onFailure {
                onFailure(it.message ?: "Delete failed")
            }
        }
    }
}
