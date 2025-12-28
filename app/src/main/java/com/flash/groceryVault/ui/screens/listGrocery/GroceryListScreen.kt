package com.flash.groceryVault.ui.screens.listGrocery

import android.content.Context
import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryListScreen(
    vm: GroceryListViewModel,
    onCreate: () -> Unit,
    onOpen: (Long) -> Unit,
    onEdit: (Long) -> Unit,
    onLoggedOut: () -> Unit,
) {
    val context = LocalContext.current
    val ui by vm.ui.collectAsState()

    // ---- Persist cloud sync status ----
    val prefs = remember { context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE) }
    LaunchedEffect(Unit) {
        val synced = prefs.getBoolean("cloud_synced", false)
        val last = prefs.getLong("cloud_last_synced_at", 0L)
        vm.restoreCloudStatus(synced, last)
    }

    // Auto-mark as "not synced" if we have local changes after last sync
    LaunchedEffect(ui.rows, ui.lastSyncedAt) {
        if (ui.lastSyncedAt <= 0L) return@LaunchedEffect
        val hasLocalNewer = ui.rows.any { it.list.updatedAt > ui.lastSyncedAt }
        if (hasLocalNewer && ui.isCloudSynced) {
            prefs.edit().putBoolean("cloud_synced", false).apply()
            vm.restoreCloudStatus(isCloudSynced = false, lastSyncedAt = ui.lastSyncedAt)
        }
    }

    // Events
    LaunchedEffect(Unit) {
        vm.events.collectLatest { e ->
            when (e) {
                is GroceryListEvent.Toast -> Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                GroceryListEvent.LoggedOut -> onLoggedOut()
            }
        }
    }

    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    var deleteListId by rememberSaveable { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GroceryVault") },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.Logout, contentDescription = "Log out")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreate) {
                Icon(Icons.Default.Add, contentDescription = "New list")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SyncCard(
                isSyncing = ui.isSyncing,
                isCloudSynced = ui.isCloudSynced,
                lastSyncedAt = ui.lastSyncedAt,
                onSync = {
                    vm.syncNow(
                        onSuccess = { now ->
                            prefs.edit()
                                .putBoolean("cloud_synced", true)
                                .putLong("cloud_last_synced_at", now)
                                .apply()
                        },
                        onFailure = {
                            prefs.edit().putBoolean("cloud_synced", false).apply()
                        }
                    )
                }
            )

            if (ui.rows.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No grocery lists yet. Tap + to create one.")
                }
            } else {
                ui.rows.forEach { row ->
                    GroceryListCard(
                        row = row,
                        onOpen = { onOpen(row.list.id) },
                        onEdit = { onEdit(row.list.id) },
                        onDelete = { deleteListId = row.list.id }
                    )
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log out?") },
            text = { Text("Do you want to log out from this account?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    vm.signOut()
                }) { Text("Log out") }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") } }
        )
    }

    deleteListId?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteListId = null },
            title = { Text("Delete list?") },
            text = { Text("This will delete the list (synced as tombstone).") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteList(
                        listId = id,
                        onSuccess = { deleteListId = null },
                        onFailure = {
                            deleteListId = null
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        }
                    )
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deleteListId = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SyncCard(
    isSyncing: Boolean,
    isCloudSynced: Boolean,
    lastSyncedAt: Long,
    onSync: () -> Unit
) {
    val label = when {
        isSyncing -> "Syncing with Cloud"
        isCloudSynced -> "Cloud Synced"
        else -> "Sync with Cloud"
    }
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleMedium)
                if (lastSyncedAt > 0L) {
                    val dt = DateFormat.format("dd MMM, HH:mm", lastSyncedAt).toString()
                    Text("Last synced: $dt", style = MaterialTheme.typography.bodySmall)
                }
            }
            OutlinedButton(onClick = onSync, enabled = !isSyncing) {
                Text("Sync")
            }
            Spacer(Modifier.width(10.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    when {
                        isSyncing -> CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        isCloudSynced -> Icon(Icons.Default.Check, contentDescription = "Synced")
                        else -> Icon(Icons.Default.CloudUpload, contentDescription = "Not synced")
                    }
                }
            }
        }
    }
}

@Composable
fun GroceryListCard(
    row: GroceryListRowUi,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val dt = DateFormat.format("dd MMM yyyy, HH:mm", row.list.createdAt).toString()
    val detail = "${row.itemCount} items • ${row.checkedCount} checked"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onOpen() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(row.list.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(dt, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(4.dp))
                Text(detail, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
        }
    }
}
