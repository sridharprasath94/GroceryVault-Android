package com.flash.groceryVault.ui.screens.detailGrocery

import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
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
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GroceryDetailTopBar(
    isInteractionEnabled: Boolean,
    checkedCount: Int,
    totalCount: Int,
    onBack: () -> Unit,
    onEdit: () -> Unit,
) {
    Box {
        TopAppBar(
            title = {
                Column {
                    Text("Grocery List")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryDetailForm(
    ui: GroceryDetailUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onToggleItemChecked: (Long, Boolean) -> Unit,
) {
    val isInteractionEnabled = !ui.isNavigating && !ui.isLoadingData
    val checkedCount = ui.groceryItems.count { it.isChecked }
    val totalCount = ui.groceryItems.size
    // UI-only sort: false (unchecked) sorts before true (checked); Kotlin sortedBy is stable
    val displayItems = ui.groceryItems.sortedBy { it.isChecked }

    var hidePurchased by rememberSaveable { mutableStateOf(false) }
    val visibleItems = if (hidePurchased) displayItems.filter { !it.isChecked } else displayItems

    Scaffold(
        topBar = {
            GroceryDetailTopBar(
                isInteractionEnabled = isInteractionEnabled,
                checkedCount = checkedCount,
                totalCount = totalCount,
                onBack = onBack,
                onEdit = onEdit
            )
        }
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
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(ui.title, style = MaterialTheme.typography.headlineSmall)
                        }

                        item {
                            Text(ui.updatedAt, style = MaterialTheme.typography.bodySmall)
                        }

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
                                    visibleItems.forEach { item ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .alpha(if (item.isChecked) 0.5f else 1f),
                                        ) {
                                            Checkbox(
                                                checked = item.isChecked,
                                                onCheckedChange = {
                                                    onToggleItemChecked(
                                                        item.id,
                                                        !item.isChecked
                                                    )
                                                }
                                            )
                                            Text(
                                                text = item.name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
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

