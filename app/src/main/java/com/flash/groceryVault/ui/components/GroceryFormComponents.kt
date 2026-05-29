package com.flash.groceryVault.ui.components

import MatchMode
import SuggestionAutoCompleteField
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
    onQuickAdd: (String) -> Unit,
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
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = padding.calculateTopPadding())
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
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

        QuickAddBar(
            suggestions = suggestions,
            showHint = groceryItems.none { it.name.isNotBlank() },
            onSubmit = onQuickAdd,
        )
    }
}

@Composable
private fun QuickAddBar(
    suggestions: List<String>,
    showHint: Boolean,
    onSubmit: (String) -> Unit,
) {
    var quickAddText by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    fun submit() {
        if (quickAddText.isBlank()) return
        onSubmit(quickAddText)
        quickAddText = ""
        focusRequester.requestFocus()
    }

    Surface(
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier
                .navigationBarsPadding()
                .imePadding()
        ) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (showHint) {
                Text(
                    "Quick add: type an item, press Enter, repeat.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuggestionAutoCompleteField(
                    value = quickAddText,
                    onValueChange = { quickAddText = it },
                    suggestions = suggestions,
                    label = "",
                    placeholder = "Quick add item...",
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    showDropdownIcon = false,
                    matchMode = MatchMode.Contains,
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                )
                IconButton(
                    onClick = { submit() },
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
            onQuickAdd = {}
        )
    }
}
