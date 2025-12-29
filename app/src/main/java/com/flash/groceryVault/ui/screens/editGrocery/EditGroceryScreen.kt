package com.flash.groceryVault.ui.screens.editGrocery

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.flash.groceryVault.data.SuggestionType
import com.flash.groceryVault.di.AppContainer
import com.flash.groceryVault.ui.components.AddRowButton
import com.flash.groceryVault.ui.components.GroceryItemFormRow
import com.flash.groceryVault.ui.components.GroceryItemRow
import com.flash.groceryVault.ui.components.SectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGroceryScreen(
    container: AppContainer,
    listId: Long,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val vm = remember(listId) { EditGroceryViewModel(container, listId) }
    val ui by vm.ui.collectAsState()
    val data by vm.data.collectAsState()

    val context = LocalContext.current

    val suggestions by container.suggestionsRepository
        .observeAllMerged(SuggestionType.GROCERY_ITEM)
        .collectAsState(initial = emptyList())

    var title by rememberSaveable { mutableStateOf("") }
    var desc by rememberSaveable { mutableStateOf("") }
    val items = remember { mutableStateListOf<GroceryItemFormRow>() }
    var initialized by remember { mutableStateOf(false) }

//    var error by remember { mutableStateOf<String?>(null) }
//    LaunchedEffect(error) {
//        error?.let {
//            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
//            error = null
//        }
//    }

    LaunchedEffect(data) {
        val d = data ?: return@LaunchedEffect
        if (!initialized) {
            title = d.list.title
            desc = d.list.description.orEmpty()
            items.clear()
            items.addAll(d.items.sortedBy { it.sortOrder }.map { GroceryItemFormRow(it.name, it.isChecked) })
            if (items.isEmpty()) items.add(GroceryItemFormRow())
            initialized = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Grocery List") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                actions = {
                    TextButton(
                        onClick = {
                            vm.save(
                                title = title,
                                description = desc.trim().ifEmpty { null },
                                items = items.map { it.name to it.isChecked },
                                onDone = onSaved,
                                onError = {
//                                    error = it
                                }
                            )
                        },
                        enabled = !ui.isSaving
                    ) { Text("Save") }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize()) {

            if (data == null) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Scaffold
            }

            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(12.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                SectionCard(title = "Groceries") {
                    items.forEachIndexed { idx, row ->
                        GroceryItemRow(
                            index = idx + 1,
                            row = row,
                            suggestions = suggestions,
                            onChange = { items[idx] = it },
                            onRemove = if (items.size > 1) ({ items.removeAt(idx) }) else null
                        )
                        if (idx != items.lastIndex) Spacer(Modifier.height(10.dp))
                    }

                    Spacer(Modifier.height(12.dp))
                    AddRowButton(
                        text = "Add item",
                        onClick = { items.add(GroceryItemFormRow()) }
                    )
                }
            }

            if (ui.isSaving) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ) {}
                    CircularProgressIndicator()
                }
            }
        }
    }
}
