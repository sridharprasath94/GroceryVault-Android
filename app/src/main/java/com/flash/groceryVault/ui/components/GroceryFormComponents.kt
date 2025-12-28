package com.flash.groceryVault.ui.components
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flash.groceryVault.ui.model.SuggestionsUi
import com.flash.groceryVault.ui.theme.GroceryVaultTheme

data class IngredientFormRow(
    val name: String = "",
    val qty: String = "",
    val unit: String = ""
)

@Composable
fun IngredientItem(
    index: Int,
    suggestions: SuggestionsUi,
    row: IngredientFormRow,
    onChange: (IngredientFormRow) -> Unit,
    onRemove: (() -> Unit)? = null,
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

        Column(
            modifier = Modifier.weight(2f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SuggestionAutoCompleteField(
                value = row.name,
                onValueChange = {
                    onChange(row.copy(name = it))
                },
                suggestions = suggestions.ingredients,
                label = "Ingredient",
                showDropdownIcon = true,
                matchMode = MatchMode.Contains
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = row.qty,
                    onValueChange = { newValue ->
                        // numbers + decimal + comma (EU)
                        val filtered =
                            newValue.filter { it.isDigit() || it == '.' || it == ',' }
                        onChange(row.copy(qty = filtered))
                    },
                    label = { Text("Qty") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    modifier = Modifier.weight(0.4f)
                )

                SuggestionAutoCompleteField(
                    modifier = Modifier.weight(0.6f),
                    value = row.unit,
                    onValueChange = { onChange(row.copy(unit = it)) },
                    suggestions = suggestions.units,
                    label = "Unit",
                    showDropdownIcon = true,
                    matchMode = MatchMode.Contains
                )
            }
        }
        if (onRemove != null) {
            IconButton(
                modifier = Modifier.weight(0.2f),
                onClick = onRemove
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove ingredient"
                )
            }
        }
    }
}

@Composable
fun StepItemRow(
    s: String,
    suggestions: SuggestionsUi,
    idx: Int,
    onChange: (String) -> Unit,
    onRemove: (() -> Unit)? = null
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
                text = idx.toString(),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SuggestionAutoCompleteField(
            modifier = Modifier.weight(2f),
            value = s,
            onValueChange = {
                onChange(it)
            },
            suggestions = suggestions.steps,
            label = "Step $idx",
            showDropdownIcon = false,
            matchMode = MatchMode.Contains
        )

        if (onRemove != null) {
            IconButton(
                modifier = Modifier.weight(0.2f),
                onClick = onRemove
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove ingredient"
                )
            }
        }
    }
}

@Preview(name = "Ingredient Item Preview", showBackground = true, widthDp = 360)
@Composable
private fun IngredientItemPreview() {
    GroceryVaultTheme {
        IngredientItem(
            index = 1,
            suggestions = SuggestionsUi(),
            row = IngredientFormRow(
                name = "Onion",
                qty = "2",
                unit = "medium"
            ),
            onChange = {},
            onRemove = {}
        )
    }
}

@Preview(name = "Step Item Row Preview", showBackground = true, widthDp = 360)
@Composable
private fun StepItemRowPreview() {
    GroceryVaultTheme {
        StepItemRow(
            s = "Chop the onions finely.",
            suggestions = SuggestionsUi(),
            idx = 1,
            onChange = {},
            onRemove = {}
        )
    }
}



