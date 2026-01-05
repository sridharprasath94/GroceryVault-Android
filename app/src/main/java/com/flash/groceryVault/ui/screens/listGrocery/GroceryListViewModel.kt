package com.flash.groceryVault.ui.screens.listGrocery

import android.text.format.DateFormat
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.groceryVault.data.SyncOrigin
import com.flash.groceryVault.di.AppContainer
import com.flash.groceryVault.ui.data.GroceryListItem
import com.flash.groceryVault.ui.util.DateFormats
import com.flash.groceryVault.ui.util.toFormattedDateTimeLegacy
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class GroceryListUiState(
    val currentUserUid: String = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous",
    val groceryListItems: List<GroceryListItem> = emptyList(),
    val showMenu: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val deleteListId: Long? = null,
    val isSyncing: Boolean = false,
    val isCloudSynced: Boolean = false,
    val lastSyncedAt: Long = 0L,
    val didAutoSync: Boolean = false,
    val isLoadingData: Boolean = false,
    val isNavigating: Boolean = false,
) {
    val showDeleteDialog: Boolean get() = deleteListId != null
    val syncLabel: String
        get() = when {
            isSyncing -> "Syncing…"
            isCloudSynced -> "Cloud Synced"
            else -> "Sync now"
        }
    val syncSupportingText: String
        get() = if (lastSyncedAt > 0L) {
            val dt = DateFormat.format(DateFormats.LIST_DATE_TIME, lastSyncedAt).toString()
            "Last synced: $dt"
        } else {
            "Not synced yet"
        }
}

sealed interface GroceryListEvent {
    object SyncNow : GroceryListEvent
    data object PerformGoogleSignOut : GroceryListEvent
    data object LoggedOut : GroceryListEvent
    data class OnOpenGroceryItem(val listId: Long) : GroceryListEvent
    data class OnEditGroceryItem(val listId: Long) : GroceryListEvent
    data class Toast(val message: String) : GroceryListEvent
}

class GroceryListViewModel(
    private val container: AppContainer,
) : ViewModel() {

    private val groceryRepository = container.groceryRepositoryForCurrentUser

    private val _ui = MutableStateFlow(GroceryListUiState())
    val ui: StateFlow<GroceryListUiState> = _ui.asStateFlow()

    private val _events = MutableSharedFlow<GroceryListEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<GroceryListEvent> = _events.asSharedFlow()

    init {
        // 1️⃣ Observe grocery lists
        viewModelScope.launch {
            groceryRepository.observeListsWithItems()
                .onStart { _ui.update { it.copy(isLoadingData = true) } }
                .distinctUntilChanged()
                .collect { entries ->
                    val rows = entries.map { entry ->
                        val checkedCount = entry.items.count { it.isChecked }
                        GroceryListItem(
                            title = entry.list.title,
                            list = entry.list,
                            updatedAtText = DateFormat.format(
                                DateFormats.LIST_DATE_TIME_WITH_YEAR,
                                entry.list.updatedAt
                            ).toString(),
                            detailText = "${entry.items.size} items • $checkedCount checked"
                        )
                    }

                    _ui.update {
                        it.copy(
                            groceryListItems = rows,
                            isLoadingData = false
                        )
                    }
                }
        }

        // 2️⃣ Observe sync origin
        viewModelScope.launch {
            groceryRepository.syncOrigin.collect { origin ->
                when (origin) {
                    SyncOrigin.Local -> {
                        _ui.update {
                            it.copy(isCloudSynced = false)
                        }
                    }

                    SyncOrigin.Remote -> {
                        // Remote updates advance lastSyncedAt and keep cloud synced
                        val latestUpdatedAt =
                            _ui.value.groceryListItems
                                .maxOfOrNull { it.list.updatedAt }
                                ?: return@collect

                        Log.d(
                            "GroceryRepository",
                            "SyncOrigin.Remote observed, updating " +
                                    "lastSyncedAt to ${latestUpdatedAt.toFormattedDateTimeLegacy()}"
                        )
                        _ui.update {
                            it.copy(
                                isCloudSynced = true,
                                lastSyncedAt = latestUpdatedAt
                            )
                        }
                    }
                }
            }
        }
    }

    fun requestEditGroceryItem(listId: Long) {
        emitIfAllowed(GroceryListEvent.OnEditGroceryItem(listId))
    }

    fun requestOpenGroceryItem(listId: Long) {
        emitIfAllowed(GroceryListEvent.OnOpenGroceryItem(listId))
    }

    fun maybeAutoSync() {
        val ui = _ui.value

        val shouldAutoSync =
            !ui.didAutoSync &&
                    !ui.isCloudSynced &&
                    ui.lastSyncedAt == 0L &&
                    ui.groceryListItems.isNotEmpty()

        if (!shouldAutoSync) return

        _ui.update { it.copy(didAutoSync = true) }
        syncNowWithCloud()
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

    fun onMenuToggle() = _ui.update { it.copy(showMenu = !it.showMenu) }
    fun onMenuDismiss() = _ui.update { it.copy(showMenu = false) }

    fun requestLogout() = _ui.update { it.copy(showMenu = false, showLogoutDialog = true) }
    fun dismissLogout() = _ui.update { it.copy(showLogoutDialog = false) }


    fun confirmLogout() {
        _ui.update { it.copy(showLogoutDialog = false, showMenu = false) }
        emitIfAllowed(GroceryListEvent.PerformGoogleSignOut)
    }

    fun onGoogleSignOutCompleted() {
        container.signOut()
        emitIfAllowed(GroceryListEvent.LoggedOut)
    }

    fun requestDelete(listId: Long) = _ui.update { it.copy(deleteListId = listId) }

    fun dismissDelete() = _ui.update { it.copy(deleteListId = null) }

    fun confirmDelete() {
        val listId = _ui.value.deleteListId ?: return
        viewModelScope.launch {
            runCatching {
                groceryRepository.deleteList(listId)
            }.onSuccess {
                _ui.update { it.copy(deleteListId = null) }
                emitIfAllowed(GroceryListEvent.Toast("Recipe deleted"))
                emitIfAllowed(GroceryListEvent.SyncNow)
            }.onFailure {
                emitIfAllowed(
                    GroceryListEvent.Toast(
                        it.message ?: "Failed to delete recipe"
                    )
                )
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
                emitIfAllowed(GroceryListEvent.Toast(msg))
                onFailure(msg)
            }
        }
    }

    fun startNavigation() {
        _ui.update { it.copy(isNavigating = true) }
    }

    fun onScreenVisible() {
        _ui.update { it.copy(isNavigating = false) }
    }


    private fun emitIfAllowed(event: GroceryListEvent) {
        if (!_ui.value.isNavigating) {
            _events.tryEmit(event)
        }
    }
}
