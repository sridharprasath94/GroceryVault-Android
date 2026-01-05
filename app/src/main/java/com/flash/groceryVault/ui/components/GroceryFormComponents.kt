package com.flash.groceryVault.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flash.groceryVault.ui.theme.GroceryVaultTheme

@Composable
fun GroceryForm(
    padding: PaddingValues,
    title: String,
    isLoading: Boolean = false,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    groceryItems: List<GroceryItemFormRow>,
    suggestions: List<String>,
    onItemChange: (index: Int, GroceryItemFormRow) -> Unit,
    onItemRemove: (index: Int) -> Unit,
    onAddItem: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
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
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = onDescriptionChange,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
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
                    OutlinedButton(onClick = onAddItem) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add item")
                    }
                }
            }
        }
    }
}

@Preview(
    name = "Grocery Form – Light",
    showBackground = true,
    widthDp = 360,
    heightDp = 720
)
@Preview(
    name = "Grocery Form – Dark",
    showBackground = true,
    widthDp = 360,
    heightDp = 720,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun GroceryFormLightPreview() {
    GroceryVaultTheme {
        GroceryForm(
            padding = PaddingValues(0.dp),
            title = "Weekly Groceries",
            isLoading = false,
            onTitleChange = {},
            description = "Things to buy this weekend",
            onDescriptionChange = {},
            groceryItems = listOf(
                GroceryItemFormRow(
                    name = "Milk",
                    isChecked = true,
                ),
                GroceryItemFormRow(
                    name = "Bread",
                    isChecked = false,
                )
            ),
            suggestions = listOf(
                "Milk",
                "Bread",
                "Eggs",
                "Rice",
                "Vegetables"
            ),
            onItemChange = { _, _ -> },
            onItemRemove = {},
            onAddItem = {}
        )
    }
}





