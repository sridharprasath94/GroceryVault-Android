package com.flash.groceryVault.ui.components

import MatchMode
import SuggestionAutoCompleteField
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flash.groceryVault.ui.theme.GroceryVaultTheme

@Composable
fun GroceryFormField(
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
                value = groceryItems.name,
                onValueChange = { onChange(groceryItems.copy(name = it)) },
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
            IconButton(
                modifier = Modifier.weight(0.2f),
                onClick = onRemove
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove item"
                )
            }
        }
    }
}

@Preview(
    name = "Grocery Field",
    showBackground = true,
    widthDp = 360
)
@Preview(
    name = "Grocery Field – Dark",
    showBackground = true,
    widthDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun GroceryFormFieldLightPreview() {
    GroceryVaultTheme {
        GroceryFormField(
            index = 1,
            groceryItems = GroceryItemFormRow(
                name = "Milk",
                isChecked = true,
            ),
            suggestions = listOf(
                "Milk",
                "Bread",
                "Eggs",
                "Rice",
                "Vegetables"
            ),
            onChange = {},
            onRemove = {},
        )
    }
}
