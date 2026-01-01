package com.flash.groceryVault.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GroceryForm(
    padding: PaddingValues,
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    groceryItems: List<GroceryItemFormRow>,
    suggestions: List<String>,
    onItemChange: (index: Int, GroceryItemFormRow) -> Unit,
    onItemRemove: (index: Int) -> Unit,
    onAddItem: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            SectionCard(title = "Groceries") {
                if (groceryItems.isEmpty()) {
                    Text(
                        "No items added.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    groceryItems.forEachIndexed { idx, row ->
                        GroceryFormField(
                            index = idx + 1,
                            groceryItems = row,
                            suggestions = suggestions,
                            onChange = { updated ->
                                onItemChange(idx, updated)
                            },
                            onRemove = { onItemRemove(idx) }
                        )

                        if (idx != groceryItems.lastIndex) {
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
        }

        item {
            AddRowButton(
                text = "Add item",
                onClick = onAddItem
            )
        }
    }
}





