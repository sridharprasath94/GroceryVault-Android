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
    val showMenu: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val pendingDeleteListId: Long? = null,
)

sealed interface GroceryListEvent {
    data class Toast(val message: String) : GroceryListEvent
    data object PerformGoogleSignOut : GroceryListEvent
    object SyncNow : GroceryListEvent
    data object LoggedOut : GroceryListEvent
}

class GroceryListViewModel(
    private val container: AppContainer,
) : ViewModel() {

    private val repo = container.groceryRepositoryForCurrentUser

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

    fun syncNowWithCloud() {
        _events.tryEmit(GroceryListEvent.SyncNow)
    }

    fun restoreCloudStatus(isCloudSynced: Boolean, lastSyncedAt: Long) {
        val validSynced = isCloudSynced && lastSyncedAt > 0L

        _ui.update {
            it.copy(
                isCloudSynced = validSynced,
                lastSyncedAt = lastSyncedAt
            )
        }
    }

    fun onMenuToggle() {
        _ui.update { it.copy(showMenu = !it.showMenu) }
    }

    fun onMenuDismiss() {
        _ui.update { it.copy(showMenu = false) }
    }

    fun requestLogout() {
        _ui.update { it.copy(showMenu = false, showLogoutDialog = true) }
    }

    fun dismissLogout() {
        _ui.update { it.copy(showLogoutDialog = false) }
    }

    fun confirmLogout() {
        _ui.update { it.copy(showLogoutDialog = false) }
        _events.tryEmit(GroceryListEvent.PerformGoogleSignOut)
    }

    fun onGoogleSignOutCompleted() {
        container.signOut()
        _events.tryEmit(GroceryListEvent.LoggedOut)
    }

    fun requestDelete(listId: Long) {
        _ui.update { it.copy(pendingDeleteListId = listId) }
    }

    fun dismissDelete() {
        _ui.update { it.copy(pendingDeleteListId = null) }
    }

    fun confirmDelete(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val listId = _ui.value.pendingDeleteListId ?: return
        viewModelScope.launch {
            runCatching {
                repo.deleteList(listId)
            }.onSuccess {
                _ui.update { it.copy(isCloudSynced = false, pendingDeleteListId = null) }
                onSuccess()
            }.onFailure {
                onFailure(it.message ?: "Delete failed")
            }
        }
    }

    fun requestSync(onSuccess: (Long) -> Unit, onFailure: (String) -> Unit) {
        if (_ui.value.isSyncing) return
        _ui.update { it.copy(isSyncing = true, showMenu = false) }

        viewModelScope.launch {
            runCatching {
                container.firestoreSyncServiceForCurrentUser().syncNow()
            }.onSuccess {
                val now = System.currentTimeMillis()
                _ui.update { it.copy(isSyncing = false, isCloudSynced = true, lastSyncedAt = now) }
                onSuccess(now)
            }.onFailure { it ->
                _ui.update { it.copy(isSyncing = false, isCloudSynced = false) }
                val msg = it.message ?: "Sync failed"
                _events.tryEmit(GroceryListEvent.Toast(msg))
                onFailure(msg)
            }
        }
    }
}
