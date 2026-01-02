@file:Suppress("DEPRECATION")

package com.flash.groceryVault.ui.screens.listGrocery

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.flash.groceryVault.ui.components.ConfirmationDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.flow.collectLatest

private const val cloudSyncedStatusKey = "cloud_synced"

private const val cloudLastSyncedTimeKey = "cloud_last_synced_at"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryListScreen(
    vm: GroceryListViewModel,
    onCreate: () -> Unit,
    onOpenGroceryItem: (Long) -> Unit,
    onEditGroceryItem: (Long) -> Unit,
    onLoggedOut: () -> Unit,
) {
    val context = LocalContext.current
    val ui by vm.ui.collectAsState()
    val prefs = remember(ui.currentUserUid) {
        context.getSharedPreferences("grocery_list_sync_${ui.currentUserUid}", Context.MODE_PRIVATE)
    }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            vm.onScreenVisible()
        }
    }
    LaunchedEffect(Unit) {
        val synced = prefs.getBoolean(cloudSyncedStatusKey, false)
        val last = prefs.getLong(cloudLastSyncedTimeKey, 0L)
        vm.restoreCloudStatus(synced, last)
    }

    LaunchedEffect(ui.groceryListItems, ui.lastSyncedAt) {
        if (ui.lastSyncedAt <= 0L) return@LaunchedEffect
        val hasLocalNewer = ui.groceryListItems.any { it.list.updatedAt > ui.lastSyncedAt }
        if (hasLocalNewer && ui.isCloudSynced) {
            prefs.edit { putBoolean(cloudSyncedStatusKey, false) }
            vm.restoreCloudStatus(isCloudSynced = false, lastSyncedAt = ui.lastSyncedAt)
        }
    }

    LaunchedEffect(ui.isCloudSynced, ui.lastSyncedAt, ui.groceryListItems) {
        vm.maybeAutoSync()
    }
    // Events
    LaunchedEffect(Unit) {
        vm.events.collectLatest { event ->
            when (event) {
                is GroceryListEvent.Toast -> Toast.makeText(
                    context,
                    event.message,
                    Toast.LENGTH_SHORT
                )
                    .show()

                is GroceryListEvent.OnEditGroceryItem -> {
                    vm.startNavigation()
                    onEditGroceryItem(event.listId)
                }

                is GroceryListEvent.OnOpenGroceryItem -> {
                    vm.startNavigation()
                    onOpenGroceryItem(event.listId)
                }

                GroceryListEvent.PerformGoogleSignOut -> {
                    val googleClient = GoogleSignIn.getClient(
                        context, GoogleSignInOptions.Builder(
                            GoogleSignInOptions.DEFAULT_SIGN_IN
                        )
                            .requestEmail()
                            .build()
                    )
                    googleClient.signOut().addOnCompleteListener {
                        vm.onGoogleSignOutCompleted()
                    }
                }

                GroceryListEvent.SyncNow -> {
                    if (!ui.isSyncing) {
                        vm.requestSync(
                            onSuccess = { now ->
                                prefs.edit {
                                    putBoolean(cloudSyncedStatusKey, true)
                                        .putLong(cloudLastSyncedTimeKey, now)
                                }
                            },
                            onFailure = {
                                prefs.edit { putBoolean(cloudSyncedStatusKey, false) }
                            }
                        )
                    }
                }

                GroceryListEvent.LoggedOut -> onLoggedOut()
            }
        }
    }


    GroceryListContent(
        ui = ui,
        onCreate = onCreate,
        onOpenGrocery = vm::requestOpenGroceryItem,
        onEditGrocery = vm::requestEditGroceryItem,
        onDeleteGrocery = vm::requestDelete,
        onMenuToggle = vm::onMenuToggle,
        onMenuDismiss = vm::onMenuDismiss,
        onSyncNow = vm::syncNowWithCloud,
        onRequestLogout = vm::requestLogout,
    )
    GroceryListDialogs(
        showLogoutDialog = ui.showLogoutDialog,
        onDismissLogout = vm::dismissLogout,
        onConfirmLogout = vm::confirmLogout,
        showDeleteDialog = ui.showDeleteDialog,
        onDismissDelete = vm::dismissDelete,
        onConfirmDelete = vm::confirmDelete,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryListContent(
    ui: GroceryListUiState,
    onCreate: () -> Unit,
    onOpenGrocery: (Long) -> Unit,
    onEditGrocery: (Long) -> Unit,
    onDeleteGrocery: (Long) -> Unit,
    onMenuToggle: () -> Unit,
    onMenuDismiss: () -> Unit,
    onSyncNow: () -> Unit,
    onRequestLogout: () -> Unit,
) {
    val isInteractionEnabled = !ui.isLoadingData && !ui.isNavigating

    Scaffold(
        topBar = {
            Box {
                TopAppBar(
                    title = { Text("Grocery List") },
                    actions = {
                        if (isInteractionEnabled) {
                            IconButton(onClick = onMenuToggle) {
                                Icon(
                                    Icons.Outlined.MoreVert,
                                    contentDescription = "More options"
                                )
                            }

                            DropdownMenu(
                                expanded = ui.showMenu,
                                onDismissRequest = onMenuDismiss
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(ui.syncLabel)
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                ui.syncSupportingText,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    },
                                    onClick = onSyncNow,
                                    trailingIcon = {
                                        Box(
                                            modifier = Modifier.padding(
                                                horizontal = 6.dp,
                                                vertical = 4.dp
                                            ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            SyncStatusIcon(
                                                isSyncing = ui.isSyncing,
                                                isCloudSynced = ui.isCloudSynced
                                            )
                                        }
                                    }
                                )

                                DropdownMenuItem(
                                    text = {
                                        Text("Log out")
                                    },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Logout,
                                            contentDescription = "Log out"
                                        )
                                    },
                                    onClick = onRequestLogout
                                )
                            }
                        }
                    }
                )

                if (!isInteractionEnabled) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .pointerInput(Unit) { /* block touches */ }
                    )
                }
            }
        },
        floatingActionButton = {
            if (isInteractionEnabled) {
                FloatingActionButton(onClick = onCreate) {
                    Icon(Icons.Default.Add, contentDescription = "New list")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (ui.groceryListItems.isEmpty()) {
                Box(
                    Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No grocery lists yet. Tap + to create one.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(ui.groceryListItems) { groceryListItem ->
                        GroceryListCard(
                            groceryListItem = groceryListItem,
                            onOpen = { onOpenGrocery(groceryListItem.list.id) },
                            onEdit = { onEditGrocery(groceryListItem.list.id) },
                            onDelete = { onDeleteGrocery(groceryListItem.list.id) }
                        )
                    }
                }
            }

            if (!isInteractionEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)
                        )
                )
            }
        }
    }
}

@Composable
fun GroceryListCard(
    groceryListItem: GroceryListItem,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onOpen() }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clickable(onClick = onOpen),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(groceryListItem.list.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(groceryListItem.createdAtText, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(4.dp))
                Text(groceryListItem.detailText, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete"
                )
            }
        }
    }
}

@Composable
fun SyncStatusIcon(
    isSyncing: Boolean,
    isCloudSynced: Boolean,
) {
    when {
        isSyncing -> {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                strokeWidth = 2.dp
            )
        }

        isCloudSynced -> {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Synced",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        else -> {
            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = "Not synced"
            )
        }
    }
}

@Composable
fun GroceryListDialogs(
    showLogoutDialog: Boolean,
    onDismissLogout: () -> Unit,
    onConfirmLogout: () -> Unit,
    showDeleteDialog: Boolean,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    ConfirmationDialog(
        show = showLogoutDialog,
        title = "Log out?",
        message = "Do you want to log out from this account?",
        confirmButtonText = "Log out",
        onConfirm = onConfirmLogout,
        onDismiss = onDismissLogout,
    )

    ConfirmationDialog(
        show = showDeleteDialog,
        title = "Delete Grocery list?",
        message = "This action cannot be undone.",
        confirmButtonText = "Delete",
        onConfirm = onConfirmDelete,
        onDismiss = onDismissDelete,
    )
}
