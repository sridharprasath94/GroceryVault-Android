package com.flash.groceryVault.ui.screens.detailGrocery

import android.text.format.DateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flash.groceryVault.di.AppContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryDetailScreen(
    container: AppContainer,
    listId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
) {
    val vm = remember(listId) { GroceryDetailViewModel(container, listId) }
    val data by vm.data.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grocery List") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                actions = { IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit") } }
            )
        }
    ) { padding ->
        if (data == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val d = data!!
        val dt = DateFormat.format("dd MMM yyyy, HH:mm", d.list.createdAt).toString()

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(d.list.title, style = MaterialTheme.typography.headlineSmall)
            Text(dt, style = MaterialTheme.typography.bodySmall)

            if (!d.list.description.isNullOrBlank()) {
                Text(d.list.description!!, style = MaterialTheme.typography.bodyLarge)
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Items", style = MaterialTheme.typography.titleMedium)

                    d.items.sortedBy { it.sortOrder }.forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = item.isChecked,
                                onCheckedChange = { vm.toggleChecked(item.id, it) }
                            )
                            Text(item.name, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}
