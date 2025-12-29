@file:Suppress("DEPRECATION")

package com.flash.groceryVault.ui.screens.listGrocery

import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest

private const val cloudSyncedStatusKey = "cloud_synced"

private const val cloudLastSyncedTimeKey = "cloud_last_synced_at"

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
    val uid = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous" }
    val prefs = remember(uid) {
        context.getSharedPreferences("grocery_list_sync_${uid}", Context.MODE_PRIVATE)
    }
    var didAutoSync by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val synced = prefs.getBoolean(cloudSyncedStatusKey, false)
        val last = prefs.getLong(cloudLastSyncedTimeKey, 0L)
        vm.restoreCloudStatus(synced, last)
    }

    LaunchedEffect(ui.rows, ui.lastSyncedAt) {
        if (ui.lastSyncedAt <= 0L) return@LaunchedEffect
        val hasLocalNewer = ui.rows.any { it.list.updatedAt > ui.lastSyncedAt }
        if (hasLocalNewer && ui.isCloudSynced) {
            prefs.edit { putBoolean(cloudSyncedStatusKey, false) }
            vm.restoreCloudStatus(isCloudSynced = false, lastSyncedAt = ui.lastSyncedAt)
        }
    }

    LaunchedEffect(ui.isCloudSynced, ui.lastSyncedAt, ui.rows) {
        if (didAutoSync) return@LaunchedEffect

        val shouldAutoSync =
            !ui.isCloudSynced && ui.lastSyncedAt == 0L && ui.rows.isNotEmpty()
        if (shouldAutoSync) {
            didAutoSync = true
            vm.syncNowWithCloud()
        }
    }
    // Events
    LaunchedEffect(Unit) {
        vm.events.collectLatest { e ->
            when (e) {
                is GroceryListEvent.Toast -> Toast.makeText(context, e.message, Toast.LENGTH_SHORT)
                    .show()

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GroceryVault") },
                actions = {
                    IconButton(onClick = vm::onMenuToggle) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = ui.showMenu,
                        onDismissRequest = vm::onMenuDismiss
                    ) {
                        val syncLabel = when {
                            ui.isSyncing -> "Syncing…"
                            ui.isCloudSynced -> "Cloud Synced"
                            else -> "Sync now"
                        }
                        val syncSupporting = if (ui.lastSyncedAt > 0L) {
                            val dt = DateFormat.format("dd MMM, HH:mm", ui.lastSyncedAt).toString()
                            "Last synced: $dt"
                        } else {
                            "Not synced yet"
                        }
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(syncLabel)
                                    Spacer(Modifier.height(2.dp))
                                    Text(syncSupporting, style = MaterialTheme.typography.bodySmall)
                                }
                            },
                            trailingIcon = {
                                Box(contentAlignment = Alignment.Center) {
                                    when {
                                        ui.isSyncing -> CircularProgressIndicator(
                                            modifier = Modifier.size(
                                                18.dp
                                            ), strokeWidth = 2.dp
                                        )

                                        ui.isCloudSynced -> Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Synced",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )

                                        else -> Icon(
                                            Icons.Default.CloudUpload,
                                            contentDescription = "Not synced"
                                        )
                                    }
                                }
                            },
                            onClick = vm::syncNowWithCloud
                        )
                        DropdownMenuItem(
                            text = { Text("Log out") },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Logout,
                                    contentDescription = "Log out"
                                )
                            },
                            onClick = { vm.requestLogout() }
                        )
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
                        onDelete = { vm.requestDelete(row.list.id) }
                    )
                }
            }
        }
    }

    if (ui.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { vm.dismissLogout() },
            title = { Text("Log out?") },
            text = { Text("Do you want to log out from this account?") },
            confirmButton = {
                TextButton(onClick = { vm.confirmLogout() }) { Text("Log out") }
            },
            dismissButton = { TextButton(onClick = { vm.dismissLogout() }) { Text("Cancel") } }
        )
    }

    ui.pendingDeleteListId?.let { _ ->
        AlertDialog(
            onDismissRequest = { vm.dismissDelete() },
            title = { Text("Delete list?") },
            text = { Text("This action cannot be undone!!!") },
            confirmButton = {
                TextButton(onClick = {
                    vm.confirmDelete(
                        onSuccess = { vm.dismissDelete() },
                        onFailure = {
                            vm.dismissDelete()
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        }
                    )
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { vm.dismissDelete() }) { Text("Cancel") } }
        )
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
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete"
                )
            }
        }
    }
}
