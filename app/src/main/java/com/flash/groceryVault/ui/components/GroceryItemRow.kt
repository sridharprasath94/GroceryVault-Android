package com.flash.groceryVault.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun GroceryItemRow(
    index: Int,
    groceryItems: GroceryItemFormRow,
    onChange: (GroceryItemFormRow) -> Unit,
    onRemove: (() -> Unit)?,
    suggestions: List<String>,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = index.toString(),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SuggestionAutoCompleteField(
                value = TextFieldValue(groceryItems.name),
                onValueChange = { onChange(groceryItems.copy(name = it.text)) },
                suggestions = suggestions,
                label = "Grocery item",
                matchMode = MatchMode.Prefix,
                showDropdownIcon = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = groceryItems.isChecked,
                    onCheckedChange = { onChange(groceryItems.copy(isChecked = it)) }
                )
                Text("Checked")
            }
        }

        if (onRemove != null) {
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove item")
            }
        }
    }
}
