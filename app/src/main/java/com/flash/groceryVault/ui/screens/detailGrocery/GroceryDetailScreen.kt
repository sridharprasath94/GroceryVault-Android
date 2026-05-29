package com.flash.groceryVault.ui.screens.detailGrocery

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import MatchMode
import SuggestionAutoCompleteField
import com.flash.groceryVault.data.GroceryItemEntity
import com.flash.groceryVault.ui.components.SectionCard
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryDetailScreen(
    vm: GroceryDetailViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
) {
    val ui by vm.ui.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            vm.onScreenVisible()
        }
    }

    LaunchedEffect(Unit) {
        vm.events.collectLatest { event ->
            when (event) {
                is GroceryDetailEvent.Toast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }

                is GroceryDetailEvent.ShowUndo -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        vm.undoRemove()
                    }
                }

                is GroceryDetailEvent.OnEditClicked -> {
                    vm.startNavigation()
                    onEdit()
                }

                GroceryDetailEvent.OnBackClicked -> {
                    vm.startNavigation()
                    onBack()
                }
            }
        }
    }


    GroceryDetailForm(
        ui = ui,
        onBack = vm::requestBack,
        onEdit = vm::requestEdit,
        onToggleItemChecked = vm::toggleChecked,
        onQuickAdd = vm::quickAddItem,
        onRemoveItem = vm::removeItem,
        onUncheckAll = vm::uncheckAll,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GroceryDetailTopBar(
    title: String,
    isInteractionEnabled: Boolean,
    checkedCount: Int,
    totalCount: Int,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onUncheckAll: () -> Unit,
) {
    Box {
        TopAppBar(
            title = {
                Column {
                    Text(title.ifBlank { "Grocery List" })
                    if (totalCount > 0) {
                        Text(
                            "$checkedCount / $totalCount done",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack, enabled = isInteractionEnabled) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                    )
                }
            },
            actions = {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                var menuExpanded by remember { mutableStateOf(false) }
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More options",
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Uncheck all") },
                        enabled = checkedCount > 0,
                        onClick = {
                            menuExpanded = false
                            onUncheckAll()
                        },
                    )
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
}

@Composable
private fun GroceryItemRow(
    item: GroceryItemEntity,
    onToggle: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .alpha(if (item.isChecked) 0.5f else 1f),
    ) {
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = { onToggle() },
        )
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove ${item.name}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryDetailForm(
    ui: GroceryDetailUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onToggleItemChecked: (Long, Boolean) -> Unit,
    onQuickAdd: (String) -> Unit,
    onRemoveItem: (GroceryItemEntity) -> Unit = {},
    onUncheckAll: () -> Unit = {},
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
) {
    val isInteractionEnabled = !ui.isNavigating && !ui.isLoadingData
    val checkedCount = ui.groceryItems.count { it.isChecked }
    val totalCount = ui.groceryItems.size
    // UI-only sort: unchecked first, newest unchecked at top; stable sort preserves group order
    val displayItems = remember(ui.groceryItems) {
        ui.groceryItems
            .sortedByDescending { it.createdAt }
            .sortedBy { it.isChecked }
    }

    var hidePurchased by rememberSaveable { mutableStateOf(false) }
    // Only recomputes when sort result or toggle changes, not on unrelated recompositions
    val visibleItems = remember(displayItems, hidePurchased) {
        if (hidePurchased) displayItems.filter { !it.isChecked } else displayItems
    }

    var quickAddText by rememberSaveable { mutableStateOf("") }
    val quickAddFocusRequester = remember { FocusRequester() }

    var purchasedExpanded by rememberSaveable { mutableStateOf(false) }
    val (activeItems, purchasedItems) = remember(visibleItems) {
        visibleItems.partition { !it.isChecked }
    }

    Scaffold(
        topBar = {
            GroceryDetailTopBar(
                title = ui.title,
                isInteractionEnabled = isInteractionEnabled,
                checkedCount = checkedCount,
                totalCount = totalCount,
                onBack = onBack,
                onEdit = onEdit,
                onUncheckAll = onUncheckAll,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (ui.isLoadingData) {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Loading…")
                    Spacer(Modifier.height(12.dp))
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                ) {
                    if (totalCount > 0) {
                        LinearProgressIndicator(
                            progress = { checkedCount.toFloat() / totalCount.toFloat() },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Hide purchased",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Switch(
                            checked = hidePurchased,
                            onCheckedChange = { hidePurchased = it },
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (!ui.description.isNullOrBlank()) {
                            item {
                                Text(ui.description, style = MaterialTheme.typography.bodyLarge)
                            }
                        }

                        item {
                            SectionCard(title = "Groceries") {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        SuggestionAutoCompleteField(
                                            value = quickAddText,
                                            onValueChange = { quickAddText = it },
                                            suggestions = ui.suggestions,
                                            label = "",
                                            placeholder = "Quick add item...",
                                            modifier = Modifier
                                                .weight(1f)
                                                .focusRequester(quickAddFocusRequester),
                                            showDropdownIcon = false,
                                            matchMode = MatchMode.Contains,
                                            keyboardActions = KeyboardActions(
                                                onDone = {
                                                    onQuickAdd(quickAddText)
                                                    quickAddText = ""
                                                    quickAddFocusRequester.requestFocus()
                                                }
                                            ),
                                        )
                                        IconButton(
                                            onClick = {
                                                onQuickAdd(quickAddText)
                                                quickAddText = ""
                                                quickAddFocusRequester.requestFocus()
                                            },
                                            enabled = quickAddText.isNotBlank(),
                                        ) {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = "Add item",
                                                tint = if (quickAddText.isNotBlank()) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                                },
                                            )
                                        }
                                    }

                                    if (visibleItems.isNotEmpty()) {
                                        HorizontalDivider()
                                    }

                                    activeItems.forEach { item ->
                                        GroceryItemRow(
                                            item = item,
                                            onToggle = { onToggleItemChecked(item.id, !item.isChecked) },
                                            onRemove = { onRemoveItem(item) },
                                        )
                                    }

                                    if (purchasedItems.isNotEmpty()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { purchasedExpanded = !purchasedExpanded }
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Text(
                                                "Purchased (${purchasedItems.size})",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                            Icon(
                                                if (purchasedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = if (purchasedExpanded) "Collapse purchased" else "Expand purchased",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        if (purchasedExpanded) {
                                            purchasedItems.forEach { item ->
                                                GroceryItemRow(
                                                    item = item,
                                                    onToggle = { onToggleItemChecked(item.id, !item.isChecked) },
                                                    onRemove = { onRemoveItem(item) },
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
