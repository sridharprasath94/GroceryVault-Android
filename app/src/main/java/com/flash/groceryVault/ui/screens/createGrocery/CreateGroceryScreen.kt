package com.flash.groceryVault.ui.screens.createGrocery

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.flash.groceryVault.ui.components.AddRowButton
import com.flash.groceryVault.ui.components.GroceryItemRow
import com.flash.groceryVault.ui.components.SectionCard
import com.flash.groceryVault.ui.components.rememberAnimatedImeBottomPadding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroceryScreen(
    vm: CreateGroceryViewModel,
    onBack: () -> Unit,
    onCreated: (Long) -> Unit,
) {
    val ui by vm.ui.collectAsState()
    val context = LocalContext.current

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            vm.onScreenVisible()
        }
    }
    LaunchedEffect(Unit) {
        vm.events.collectLatest { event ->
            when (event) {
                is CreateGroceryEvent.Toast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }

                is CreateGroceryEvent.OnFinishedSaving -> {
                    vm.startNavigation()
                    onCreated(event.id)
                }

                CreateGroceryEvent.OnBackClicked -> {
                    vm.startNavigation()
                    onBack()
                }
            }
        }
    }

    val imePadding = rememberAnimatedImeBottomPadding()
    Scaffold(
        modifier = Modifier.padding(bottom = imePadding),
        topBar = {
            TopAppBar(
                title = { Text("New Grocery List") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            keyboardController?.hide()
                            vm.save()

                        },
                        enabled = !ui.isSaving
                    ) { Text("Save") }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(12.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = ui.title,
                        onValueChange = vm::updateTitle,
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = ui.description,
                        onValueChange = vm::updateDescription,
                        label = { Text("Notes (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    SectionCard(title = "Groceries") {
                        ui.groceryItems.forEachIndexed { idx, row ->
                            GroceryItemRow(
                                index = idx + 1,
                                groceryItems = row,
                                suggestions = ui.suggestions,
                                onChange = { vm.onGroceryItemChanged(idx, row) },
                                onRemove = { vm.onGroceryItemRemoved(idx) }
                            )
                            if (idx != ui.groceryItems.lastIndex) {
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
                }

                item {
                    AddRowButton(
                        text = "Add item",
                        onClick = {
                            vm.onAddGroceryItem()
                            scope.launch {
                                listState.animateScrollToItem(ui.groceryItems.lastIndex)
                            }
                        }
                    )
                }
            }

            if (ui.isSaving) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)
                        )
                )
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
